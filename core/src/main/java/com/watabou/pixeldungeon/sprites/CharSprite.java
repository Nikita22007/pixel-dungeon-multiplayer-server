/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Game;
import com.nikita22007.multiplayer.noosa.MovieClip;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.EmoIcon;
import com.watabou.pixeldungeon.effects.FloatingText;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.effects.TorchHalo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.items.potions.PotionOfInvisibility;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.json.JSONObject;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.watabou.pixeldungeon.network.SendData.sendCharSpriteAction;
import static com.watabou.pixeldungeon.network.SendData.sendCharSpriteState;

public class CharSprite extends MovieClip implements Tweener.Listener, MovieClip.Listener {

	public static final int DEFAULT		= 0xFFFFFF;
	public static final int POSITIVE	= 0x00FF00;
	public static final int NEGATIVE	= 0xFF0000;
	public static final int WARNING		= 0xFF8800;
	public static final int NEUTRAL		= 0xFFFF00;

	private static final float MOVE_INTERVAL	= 0.1f;
	private static final float FLASH_INTERVAL	= 0.05f;

	protected void setEmo(EmoIcon emo) {
		this.emo = emo;
		if (this.ch != null) {
			SendData.sendActor(this.ch);
		}
	}

	public JSONObject getEmoJsonObject() {
		if (emo == null){
			return new JSONObject();
		}
		return emo.toJsonObject();
	}

	public enum State {
		BURNING, LEVITATING, INVISIBLE, PARALYSED, FROZEN, ILLUMINATED
	}

	protected final Set<State> states = new CopyOnWriteArraySet<State>();

	protected Animation idle;
	protected Animation run;
	protected Animation attack;
	protected Animation operate;
	protected Animation zap;
	protected Animation die;

	protected Callback animCallback;

	protected Tweener motion;

	protected Emitter burning;
	protected Emitter levitation;

	protected TorchHalo halo;

	protected EmoIcon emo;

	private Callback jumpCallback;

	private float flashTime = 0;

	protected boolean sleeping = false;

	public Char ch;

	public CharSprite() {
		super();
		listener = this;
	}

	public Set<State> states(){
		return states;
	}

	public String spriteName() {
		return Utils.toSnakeCase(this.getClass().getSimpleName());
	}


	public void link( Char ch ) {
		this.ch = ch;
		ch.setSprite(this);

		place( ch.pos );
		turnTo( ch.pos, Random.Int( Level.LENGTH ) );

		ch.updateSpriteState();
	}

	public PointF worldToCamera( int cell ) {

		final int csize = DungeonTilemap.SIZE;

		return new PointF(
			((cell % Level.WIDTH) + 0.5f) * csize ,
			((cell / Level.WIDTH) + 1.0f) * csize
		);
	}

	public void place( int cell ) {
		sendCharSpriteAction(ch.id(), "place", null, cell);
		point( worldToCamera( cell ) );
	}

	public void showStatus( int color, String text, Object... args ) {
		if (visible) {
			if (args.length > 0) {
				text = Utils.format( text, args );
			}
			x += width * 0.5f;
			Integer key;
			if (ch != null) {
				FloatingText.show( x, y, ch.pos, text, color );
				key = ch.pos;
			} else {
				FloatingText.show( x, y, text, color );
				key = null;
			}
			SendData.addToSendShowStatus(
					x,
					y,
					key,
					text,
					color,
					true
			);
		}
	}

	public void idle() {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "idle", null, null);
		}
		play( idle );
	}

	public void move( int from, int to ) {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "run", from, to);
		}
		play( run );

		turnTo( from , to );

		if (visible && Level.water[from] && !ch.flying) {
			GameScene.ripple( from );
		}

		ch.onMotionComplete();
	}

	public void interruptMotion() {
		if (motion != null) {
			onComplete( motion );
		}
	}

	public void attack( int cell ) {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "attack", null, cell);
		}
		turnTo( ch.pos, cell );
		play( attack );
	}

	public void attack( int cell, Callback callback ) {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "attack", null, cell);
		}
		animCallback = callback;
		turnTo( ch.pos, cell );
		play( attack );
	}

	public void operate( int cell ) {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "operate", null, cell);
		}
		turnTo( ch.pos, cell );
		play( operate );
	}

	public void zap( int cell ) {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "zap", null, cell);
		}
		turnTo( ch.pos, cell );
		play( zap );
	}

	public void turnTo( int from, int to ) {
		int fx = from % Level.WIDTH;
		int tx = to % Level.WIDTH;
		if (tx > fx) {
			flipHorizontal = false;
		} else if (tx < fx) {
			flipHorizontal = true;
		}
	}

	public void jump( int from, int to, Callback callback ) {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "jump", from, to);
		}
		jumpCallback = callback;

		turnTo( from, to );

		if (visible && Level.water[ch.pos] && !ch.flying) {
			GameScene.ripple( ch.pos );
		}
		if (jumpCallback != null) {
			jumpCallback.call();
		}

	}

	public void read(){
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "read", null, null);
		}
	}

	public void die() {
		if (ch != null) {
			sendCharSpriteAction(ch.id(), "die", null, null);
		}
		sleeping = false;
		play( die );

		if (emo != null) {
			emo.killAndErase();
		}
	}

	public Emitter emitter() {
		Emitter emitter = GameScene.emitter();
		emitter.pos( this );
		return emitter;
	}

	public Emitter centerEmitter() {
		Emitter emitter = GameScene.emitter();
		emitter.pos( center() );
		return emitter;
	}

	public Emitter bottomEmitter() {
		Emitter emitter = GameScene.emitter();
		emitter.pos( x, y + height, width, 0 );
		return emitter;
	}

	public void burst( final int color, int n ) {
		if (visible) {
			Splash.at( center(), color, n );
		}
	}

	public void bloodBurstA( PointF from, int damage ) {
		if (visible) {
			PointF c = center();
			int n = (int)Math.min( 9 * Math.sqrt( (double)damage / ch.getHT()), 9 );
			Splash.at( c, PointF.angle( from, c ), 3.1415926f / 2, blood(), n );
		}
	}

	public int blood() {
		return 0xFFBB0000;
	}

	public void flash() {
		flashTime = FLASH_INTERVAL;
		SendData.sendFlashChar(this, flashTime);
	}

	public void add( State state ) {
		states.add(state);
		sendCharSpriteState(ch, state, false);
		switch (state) {
		case BURNING:
			burning = emitter();
			burning.pour( FlameParticle.FACTORY, 0.06f );
			if (visible) {
				Sample.INSTANCE.play( Assets.SND_BURNING );
			}
			break;
		case LEVITATING:
			levitation = emitter();
			levitation.pour( Speck.factory( Speck.JET ), 0.02f );
			break;
		case INVISIBLE:
			PotionOfInvisibility.melt( ch );
			break;
		case PARALYSED:
			paused = true;
			break;
		case FROZEN:
			paused = true;
			break;
		case ILLUMINATED:
			GameScene.effect( halo = new TorchHalo( this ) );
			break;
		}
	}

	public void remove( State state ) {
		states.remove(state);
		sendCharSpriteState(ch, state, true);
		switch (state) {
		case BURNING:
			if (burning != null) {
				burning.on = false;
				burning = null;
			}
			break;
		case LEVITATING:
			if (levitation != null) {
				levitation.on = false;
				levitation = null;
			}
			break;
		case INVISIBLE:
			break;
		case PARALYSED:
			paused = false;
			break;
		case FROZEN:
			paused = false;
			break;
		case ILLUMINATED:
			if (halo != null) {
				halo.putOut();
			}
			break;
		}
	}

	@Override
	public void update() {

		super.update();

		if (paused && listener != null) {
			listener.onComplete( curAnim );
		}

		if (burning != null) {
			burning.visible = visible;
		}
		if (levitation != null) {
			levitation.visible = visible;
		}
		if (sleeping) {
			showSleep();
		} else {
			hideSleep();
		}
		if (emo != null) {
			emo.visible = visible;
		}
	}

	public void showSleep() {
		if (emo instanceof EmoIcon.Sleep) {

		} else {
			if (emo != null) {
				emo.killAndErase();
			}
			setEmo(new EmoIcon.Sleep( this ));
		}
	}

	public void hideSleep() {
		if (emo instanceof EmoIcon.Sleep) {
			emo.killAndErase();
			setEmo(null);
		}
	}

	public void showAlert() {
		if (emo instanceof EmoIcon.Alert) {

		} else {
			if (emo != null) {
				emo.killAndErase();
			}
			setEmo(new EmoIcon.Alert( this ));
		}
	}

	public void hideAlert() {
		if (emo instanceof EmoIcon.Alert) {
			emo.killAndErase();
			setEmo(null);
		}
	}

	@Override
	public void kill() {
		super.kill();

		if (emo != null) {
			emo.killAndErase();
			setEmo(null);
		}
	}

	@Override
	public void onComplete( Tweener tweener ) {
	if (tweener == motion) {

			motion.killAndErase();
			motion = null;
		}
	}

	@Override
	public void onComplete( Animation anim ) {

		if (animCallback != null) {
			animCallback.call();
			animCallback = null;
		} else {

			if (anim == attack) {

				idle();
				ch.onAttackComplete();

			} else if (anim == operate) {

				idle();
				ch.onOperateComplete();

			}

		}
	}

	private static class JumpTweener extends Tweener {

		public Visual visual;

		public PointF start;
		public PointF end;

		public float height;

		public JumpTweener( Visual visual, PointF pos, float height, float time ) {
			super( visual, time );

			this.visual = visual;
			start = visual.point();
			end = pos;

			this.height = height;
		}

		@Override
		protected void updateValues( float progress ) {
			visual.point( PointF.inter( start, end, progress ).offset( 0, -height * 4 * progress * (1 - progress) ) );
		}
	}
}

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
package com.watabou.pixeldungeon.ui;

import com.watabou.input.Touchscreen.Touch;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.BitmaskEmitter;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.BloodParticle;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.HeroSprite;
import com.watabou.pixeldungeon.windows.WndGame;

public class StatusPane extends Component {    //remove when server is not client
	
	private NinePatch shield;
	private Image avatar;
	private Emitter blood;
	
	private int lastTier = 0;
	
	private Image hp;
	private Image exp;
	
	private int lastLvl = -1;
	private int lastKeys = -1;
	
	private BitmapText level;
	private BitmapText depth;
	private BitmapText keys;

	private BuffIndicator buffs;
	private Compass compass;
	
	private MenuButton btnMenu;
	
	@Override
	protected void createChildren() {
		
		shield = new NinePatch( Assets.STATUS, 80, 0, 30   + 18, 0 );
		add( shield );
		
		add( new TouchArea( 0, 1, 30, 30 ) {
			@Override
			protected void onClick( Touch touch ) {
				Image sprite = Dungeon.heroes[0].sprite;
				if (!sprite.isVisible()) {
					Camera.main.focusOn( sprite );
				}
			};			
		} );
		
		btnMenu = new MenuButton();
		add( btnMenu );
		
		avatar = HeroSprite.avatar( Dungeon.heroes[0].heroClass, lastTier );
		add( avatar );
		
		blood = new BitmaskEmitter( avatar );
		blood.pour( BloodParticle.FACTORY, 0.3f );
		blood.autoKill = false;
		blood.on = false;
		add( blood );
		
		compass = new Compass( Dungeon.level.exit );
		add( compass );
		
		hp = new Image( Assets.HP_BAR );	
		add( hp );
		
		exp = new Image( Assets.XP_BAR );
		add( exp );
		
		level = new BitmapText( PixelScene.font1x );
		level.hardlight( 0xFFEBA4 );
		add( level );
		
		depth = new BitmapText( Integer.toString( Dungeon.depth ), PixelScene.font1x );
		depth.hardlight( 0xCACFC2 );
		depth.measure();
		add( depth );
		
		Dungeon.heroes[0].belongings.countIronKeys();
		keys = new BitmapText( PixelScene.font1x );
		keys.hardlight( 0xCACFC2 );
		add( keys );

		buffs = new BuffIndicator( Dungeon.heroes[0] );
		add( buffs );
	}
	
	@Override
	protected void layout() {
		
		height = 32;
		
		shield.size( width, shield.height );
		
		avatar.x = PixelScene.align( camera(), shield.x + 15 - avatar.width / 2 );
		avatar.y = PixelScene.align( camera(), shield.y + 16 - avatar.height / 2 );
		
		compass.x = avatar.x + avatar.width / 2 - compass.origin.x;
		compass.y = avatar.y + avatar.height / 2 - compass.origin.y;
		
		hp.x = 30;
		hp.y = 3;
		
		depth.x = width - 24 - depth.width()    - 18;
		depth.y = 6;
		
		keys.y = 6;

		buffs.setPos( 32, 11 );
		
		btnMenu.setPos( width - btnMenu.width(), 1 );
	}

	@Override
	public void update() {
		super.update();

		float health = (float)Dungeon.heroes[0].HP / Dungeon.heroes[0].HT;
		
		if (health == 0) {
			avatar.tint( 0x000000, 0.6f );
			blood.on = false;
		} else if (health < 0.25f) {
			avatar.tint( 0xcc0000, 0.4f );
			blood.on = true;
		} else {
			avatar.resetColor();
			blood.on = false;
		}
		
		hp.scale.x = health;
		exp.scale.x = (width / exp.width) * Dungeon.heroes[0].exp / Dungeon.heroes[0].maxExp();
		
		if (Dungeon.heroes[0].lvl != lastLvl) {
			
			if (lastLvl != -1) {
				Emitter emitter = (Emitter)recycle( Emitter.class );
				emitter.revive();
				emitter.pos( 27, 27 );
				emitter.burst( Speck.factory( Speck.STAR ), 12 );
			}
			
			lastLvl = Dungeon.heroes[0].lvl;
			level.text( Integer.toString( lastLvl ) );
			level.measure();
			level.x = PixelScene.align( 27.5f - level.width() / 2 );
			level.y = PixelScene.align( 28.0f - level.baseLine() / 2 );
		}
		
		int k = IronKey.curDepthQuantity;
		if (k != lastKeys) {
			lastKeys = k;
			keys.text( Integer.toString( lastKeys ) );
			keys.measure();
			keys.x = width - 8 - keys.width()    - 18;
		}
		
		int tier = Dungeon.heroes[0].tier();
		if (tier != lastTier) {
			lastTier = tier;
			avatar.copy( HeroSprite.avatar( Dungeon.heroes[0].heroClass, tier ) );
		}
	}
	
	private static class MenuButton extends Button {
		
		private Image image;
		
		public MenuButton() {
			super();
			
			width = image.width + 4;
			height = image.height + 4;
		}
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			image = new Image( Assets.STATUS, 114, 3, 12, 11 );
			add( image );
		}
		
		@Override
		protected void layout() {
			super.layout();
			
			image.x = x + 2;
			image.y = y + 2;
		}
		
		@Override
		protected void onTouchDown() {
			image.brightness( 1.5f );
			Sample.INSTANCE.play( Assets.SND_CLICK );
		}
		
		@Override
		protected void onTouchUp() {
			image.resetColor();
		}
		
		@Override
		protected void onClick() {
			GameScene.show( new WndGame() );
		}
	}
}

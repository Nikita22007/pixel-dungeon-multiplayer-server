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
package com.watabou.pixeldungeon.windows;

import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteGlowing;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class WndOptions extends Window {

	private static final int WIDTH			= 120;
	private static final int MARGIN 		= 2;
	private static final int BUTTON_HEIGHT	= 20;

	public WndOptions(Hero owner, String title, String message, String... options) {
		super(owner);
		sendWnd(owner, null, null, title, message, options);
	}

	public WndOptions(@NotNull Hero owner, int icon, @Nullable ItemSpriteGlowing iconGlowing, String title, String message, String... options) {
		super(owner);
		sendWnd(owner, icon, iconGlowing, title, message, options);
	}

	protected void sendWnd(@NotNull Hero owner, @Nullable Integer icon, @Nullable ItemSpriteGlowing iconGlowing, @NotNull String title, String message, @NotNull String... options) {
		JSONObject params = new JSONObject();
		try {
			params.put("title", title);
			params.put("message", message);
			JSONArray optionsArr = new JSONArray();
			for (int i = 0; i < options.length; i += 1) {
				optionsArr.put(options[i]);
			}
			params.put("options", optionsArr);
			if (icon != null) {
				params.put("icon", icon);
				params.put("icon_glowing", iconGlowing == null ? JSONObject.NULL : iconGlowing.toJsonObject());
			}
		} catch (JSONException ignored) {
		}
		SendData.sendWindow(owner.networkID, "wnd_option", getId(), params);
	}

	public WndOptions(String title, String message, String... options) {
		init(title, message, options);
	}
	protected void init(String title, String message, String... options ) {
		BitmapTextMultiline tfTitle = PixelScene.createMultiline( title, 9 );
		tfTitle.hardlight( TITLE_COLOR );
		tfTitle.x = tfTitle.y = MARGIN;
		tfTitle.maxWidth = WIDTH - MARGIN * 2;
		tfTitle.measure();
		add( tfTitle );
		
		BitmapTextMultiline tfMesage = PixelScene.createMultiline( message, 8 );
		tfMesage.maxWidth = WIDTH - MARGIN * 2;
		tfMesage.measure();
		tfMesage.x = MARGIN;
		tfMesage.y = tfTitle.y + tfTitle.height() + MARGIN;
		add( tfMesage );
		
		float pos = tfMesage.y + tfMesage.height() + MARGIN;
		
		for (int i=0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton( options[i] ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			btn.setRect( MARGIN, pos, WIDTH - MARGIN * 2, BUTTON_HEIGHT );
			add( btn );
			
			pos += BUTTON_HEIGHT + MARGIN;
		}
		
		resize( WIDTH, (int)pos );
	}

	protected WndOptions(Hero hero){
		super(hero);
	};

	protected abstract void onSelect( int index );
}

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

import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.HighlightedText;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WndQuest extends Window {

	private static final int WIDTH_P	= 120;
	private static final int WIDTH_L	= 144;
	
	private static final int BTN_HEIGHT	= 20;
	private static final int GAP		= 2;

	public WndQuest(Hero owner, NPC questgiver, String text, String... options ) {
		super(owner);

		int width = PixelDungeon.landscape() ? WIDTH_L : WIDTH_P;

		CharSprite questgiverSprie = questgiver.sprite();
		String title = Utils.capitalize( questgiver.name );
		IconTitle titlebar = new IconTitle( questgiverSprie, title );
		titlebar.setRect( 0, 0, width, 0 );
		add( titlebar );
		
		HighlightedText hl = new HighlightedText( 6 );
		hl.text( text, width );
		hl.setPos( titlebar.left(), titlebar.bottom() + GAP );
		add( hl );
		
		if (options.length > 0) {
			float pos = hl.bottom();
			
			for (int i=0; i < options.length; i++) {
				
				pos += GAP;
				
				final int index = i;
				RedButton btn = new RedButton( options[i] ) {
					@Override
					protected void onClick() {
						hide();
						onSelect( index );
					}
				};
				btn.setRect( 0, pos, width, BTN_HEIGHT );
				add( btn );
				
				pos += BTN_HEIGHT;
			}
			
			resize( width, (int)pos );
			
		} else {
		
			resize( width, (int)hl.bottom() );
		}



		JSONObject params = new JSONObject();
		try {
			params.put("title", title);
			params.put("text", text);
			params.put("sprite", questgiverSprie.spriteName());
			JSONArray optionsArr = new JSONArray();
			for (int i = 0; i < options.length; i += 1) {
				optionsArr.put(options[i]);
			}
			params.put("options", optionsArr);
		} catch (JSONException ignored) {}
		SendData.sendWindow(owner.networkID, "wnd_quest", getId(), params);
	}
	
	protected void onSelect( int index ) {};
}

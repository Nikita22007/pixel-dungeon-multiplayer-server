package com.watabou.pixeldungeon.scenes;

import com.watabou.noosa.BitmapText;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.network.Scanner;
import com.watabou.pixeldungeon.network.ServerInfo;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.Archs;
import com.watabou.pixeldungeon.ui.ExitButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.WndConnectServer;
import com.watabou.pixeldungeon.windows.WndError;
import com.watabou.pixeldungeon.windows.WndOptions;

import java.util.List;


/* TODO
    * Add UpdateButton
    * Add Next/Previous buttons to navigate on server  menu
    * TABLE_SIZE should be calculated based on the height of the screen
*/

public class ConnectScene extends PixelScene {

    private static final int DEFAULT_COLOR	= 0xCCCCCC;
    private static final int TABLE_SIZE=6;

    private static final String TXT_TITLE		= "Servers";
    private static final String TXT_NO_GAMES	= "No servers found.";
    private static final String TXT_ERROR	    = "Servers search error.";
    private static final String TXT_SEARCHING	= "Searching...";
    private static final String TXT_WIFI_DISABLED	= "WI-FI is not connected.";

    private static final float ROW_HEIGHT_L	= 22;
    private static final float ROW_HEIGHT_P	= 28;

    private static final float MAX_ROW_WIDTH	= 180;

    private static final float GAP	= 4;

    private Archs archs;

    private Group page;

    @Override
    public void create() {

        super.create();

        Music.INSTANCE.play( Assets.THEME, true );
        Music.INSTANCE.volume( 1f );

        uiCamera.visible = false;

        int w = Camera.main.width;
        int h = Camera.main.height;

        archs = new Archs();
        archs.setSize( w, h );
        add( archs );
        if (!Scanner.isWifiConnected()){
            BitmapText title = PixelScene.createText( TXT_WIFI_DISABLED, 8 );
            title.hardlight( DEFAULT_COLOR );
            title.measure();
            title.x = align( (w - title.width()) / 2 );
            title.y = align( (h - title.height()) / 2 );
            add( title );
        }
        else {
            if (!Scanner.start()) {

                BitmapText title = PixelScene.createText(TXT_ERROR, 8);
                title.hardlight(DEFAULT_COLOR);
                title.measure();
                title.x = align((w - title.width()) / 2);
                title.y = align((h - title.height()) / 2);
                add(title);

            } else {
                ServerInfo[] serverList;

                List<ServerInfo> list;
                list=Scanner.getServerList();
                serverList=list.toArray(new ServerInfo[list.size()]); //Todo use only List<?>
                if (serverList.length > 0) {

                    float rowHeight = PixelDungeon.landscape() ? ROW_HEIGHT_L : ROW_HEIGHT_P;

                    float left = (w - Math.min(MAX_ROW_WIDTH, w)) / 2 + GAP;
                    float top = align((h - rowHeight * serverList.length) / 2);

                    BitmapText title = PixelScene.createText(TXT_TITLE, 9);
                    title.hardlight(Window.TITLE_COLOR);
                    title.measure();
                    title.x = align((w - title.width()) / 2);
                    title.y = align(top - title.height() - GAP);
                    add(title);

                    int pos = 0;

                    for (int i = 0; i < TABLE_SIZE; i += 1) {
                        Record row = new Record(pos, false, serverList[i], this);
                        row.setRect(left, top + pos * rowHeight, w - left * 2, rowHeight);
                        add(row);

                        pos++;
                    }

                    if (serverList.length > TABLE_SIZE) {
                        //todo previous/next
                    }

                } else {

                    BitmapText title = PixelScene.createText(TXT_SEARCHING, 8);
                    title.hardlight(DEFAULT_COLOR);
                    title.measure();
                    title.x = align((w - title.width()) / 2);
                    title.y = align((h - title.height()) / 2);
                    add(title);

                }
            }
        }
        ExitButton btnExit = new ExitButton(){
            @Override
            public void onClick(){ onBackPressed(); }
        };
        btnExit.setPos( Camera.main.width - btnExit.width(), 0 );
        add( btnExit );

        fadeIn();
    }

    @Override
    protected void onBackPressed() {
        Scanner.stop();
        PixelDungeon.switchNoFade( TitleScene.class );
    }

    public static class Record extends Button {
        public Scene ConnectScene;

        private static final float GAP	= 4;

        private static final int TEXT_WIN	= 0xFFFF88;
        private static final int TEXT_LOSE	= 0xCCCCCC;
        private static final int FLARE_WIN	= 0x888866;
        private static final int FLARE_LOSE	= 0x666666;

        private ServerInfo rec;

        private ItemSprite shield;
        private Flare flare;
        private BitmapText position;
        private BitmapTextMultiline desc;
        private Image classIcon;

        public Record( int pos, boolean withFlare, ServerInfo rec,Scene scene ) {
            super();
            this.ConnectScene=scene;
            this.rec = rec;

            if (withFlare) {
                flare = new Flare( 6, 24 );
                flare.angularSpeed = 90;
              //  flare.color( rec.win ? FLARE_WIN : FLARE_LOSE );
                flare.color( FLARE_WIN);
                addToBack( flare );
            }

            position.text( Integer.toString( pos+1 ) );
            position.measure();

            desc.text( rec.name );
            desc.measure();

            if (rec.haveChallenges) {
                shield.view( ItemSpriteSheet.AMULET, null );
                position.hardlight( TEXT_WIN );
                desc.hardlight( TEXT_WIN );
            } else {
                position.hardlight( TEXT_LOSE );
                desc.hardlight( TEXT_LOSE );
            }

            classIcon.copy( Icons.get( Icons.PREFS ) );
        }

        @Override
        protected void createChildren() {

            super.createChildren();

            shield = new ItemSprite( ItemSpriteSheet.CHEST, null );
            add( shield );

            position = new BitmapText( PixelScene.font1x );
            add( position );

            desc = createMultiline( 9 );
            add( desc );

            classIcon = new Image();
            add( classIcon );
        }

        @Override
        protected void layout() {

            super.layout();

            shield.x = x;
            shield.y = y + (height - shield.height) / 2;

            position.x = align( shield.x + (shield.width - position.width()) / 2 );
            position.y = align( shield.y + (shield.height - position.height()) / 2 + 1 );

            if (flare != null) {
                flare.point( shield.center() );
            }

            classIcon.x = align( x + width - classIcon.width );
            classIcon.y = shield.y;

            desc.x = shield.x + shield.width + GAP;
            desc.maxWidth = (int)(classIcon.x - desc.x);
            desc.measure();
            desc.y = position.y + position.baseLine() - desc.baseLine();
        }

        @Override
        protected void onClick() {
            this.ConnectScene.add( new WndConnectServer(rec.name,rec.players,rec.maxPlayers, rec.IP.toString(),rec.port) );
        }
    }
}

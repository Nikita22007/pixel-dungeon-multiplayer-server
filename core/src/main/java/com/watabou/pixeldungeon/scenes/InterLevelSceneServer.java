package com.watabou.pixeldungeon.scenes;

import android.net.Network;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Network.SendData;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.windows.WndStory;

public class InterLevelSceneServer {
    private static final float TIME_TO_FADE = 0.3f;

    private static final String TXT_DESCENDING	= "Descending...";
    private static final String TXT_ASCENDING	= "Ascending...";
    private static final String TXT_LOADING		= "Loading...";
    private static final String TXT_RESURRECTING= "Resurrecting...";
    private static final String TXT_RETURNING	= "Returning...";
    private static final String TXT_FALLING		= "Falling...";
    private static final String TXT_INCORRECT_MODE = "Incorrect Interlevel scene mode";

    private static final String ERR_FILE_NOT_FOUND	= "File not found. For some reason.";
    private static final String ERR_GENERIC			= "Something went wrong..."	;

    public static int returnDepth;
    public static int returnPos;

    public static boolean noStory = false;

    public static boolean fallIntoPit;

    public static void descend(Hero hero) throws Exception {// спуск
        for (int i=0;i<Dungeon.heroes.length;i++) {
            SendData.sendInterLevelSceneDescend(i);
        }
        Actor.fixTime();
        /*if (Dungeon.hero == null) {
            Dungeon.init();
            if (noStory) {
                Dungeon.chapters.add( WndStory.ID_SEWERS );
                noStory = false;
            }
            GameLog.wipe();
        } else {*/
        if (Dungeon.depth>0) {
            Dungeon.saveLevel();
        }
        //}

        Level level;
        level=getNextLevel();
        Dungeon.switchLevel( level, level.entrance, hero );
    }
    public static  void  fall(Hero  hero)throws Exception {
     fall(hero,false);
    }
    public static void fall(Hero hero, boolean fallIntoPit) throws Exception {

        Actor.fixTime();
        Dungeon.saveLevel();

        Level level;
        level=getNextLevel();
        Dungeon.switchLevel( level, fallIntoPit ? level.pitCell() : level.randomRespawnCell(),hero );
    }
    private static Level getNextLevel()throws Exception {

        if (Dungeon.depth >= Statistics.deepestFloor) {
            return  Dungeon.newLevel();
        } else {
            Dungeon.depth++;
            return Dungeon.loadLevel();
        }
    };

    public static void ascend(Hero hero) throws Exception {
        Actor.fixTime();

        Dungeon.saveLevel();
        Dungeon.depth--;
        Level level = Dungeon.loadLevel();
        Dungeon.switchLevel( level, level.exit );
    }

    public static void returnTo(int  depth, int pos, Hero  hero) throws Exception {

        Actor.fixTime();

        Dungeon.saveLevel();
        Dungeon.depth = returnDepth;
        Level level = Dungeon.loadLevel();
        Dungeon.switchLevel( level, Level.resizingNeeded ? level.adjustPos( returnPos ) : returnPos );
    }

    private void restore() throws Exception {

        Actor.fixTime();

        GameLog.wipe();

        Dungeon.loadGame( StartScene.curClass );
        if (Dungeon.depth == -1) {
            Dungeon.depth = Statistics.deepestFloor;
            Dungeon.switchLevel( Dungeon.loadLevel( StartScene.curClass ), -1 );
        } else {
            Level level = Dungeon.loadLevel( StartScene.curClass );
            Dungeon.switchLevel( level, Level.resizingNeeded ? level.adjustPos( Dungeon.hero.pos ) : Dungeon.hero.pos );
        }
    }

    public static void resurrect() throws Exception {

        Actor.fixTime();

        if (Dungeon.bossLevel(Dungeon.depth)) {
            Dungeon.hero.resurrect( Dungeon.depth );
            Dungeon.depth--;
          //  Level level = Dungeon.newLevel();
         //   Dungeon.switchLevel( level, level.entrance );
        } else {
            Dungeon.hero.resurrect( -1 );
         //   Dungeon.resetLevel();
        }

        int pos = Dungeon.level.getSpawnCell(hero);
        if (pos!=-1){
            WandOfBlink.appear(hero);
        }
    }

}

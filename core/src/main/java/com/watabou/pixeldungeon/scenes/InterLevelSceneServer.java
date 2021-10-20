package com.watabou.pixeldungeon.scenes;

import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.network.SendData;
import com.watabou.pixeldungeon.Settings;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndStory;
import com.watabou.utils.Random;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.watabou.pixeldungeon.Dungeon.heroes;
import static com.watabou.pixeldungeon.Dungeon.switchLevel;
import static com.watabou.pixeldungeon.levels.Level.getNearClearCell;

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

    private static final String TXT_WELCOME			= "Welcome to the level %d of Pixel Dungeon!";
    private static final String TXT_WELCOME_BACK	= "Welcome back to the level %d of Pixel Dungeon!";
    private static final String TXT_NIGHT_MODE		= "Be cautious, since the dungeon is even more dangerous at night!";

    private static final String TXT_CHASM	= "Your steps echo across the dungeon.";
    private static final String TXT_WATER	= "You hear the water splashing around you.";
    private static final String TXT_GRASS	= "The smell of vegetation is thick in the air.";
    private static final String TXT_SECRETS	= "The atmosphere hints that this floor hides many secrets.";

    private static void ShowStoryIfNeed(int depth)
    {
        if (Statistics.deepestFloor>=depth){return;}
        switch (depth) { //Dungeon.depth
            case 1:
                SendData.sendWindowStory( WndStory.ID_SEWERS );
                break;
            case 6:
                SendData.sendWindowStory( WndStory.ID_PRISON );
                break;
            case 11:
                SendData.sendWindowStory( WndStory.ID_CAVES );
                break;
            case 16:
                SendData.sendWindowStory( WndStory.ID_METROPOLIS );
                break;
            case 22:
                SendData.sendWindowStory( WndStory.ID_HALLS );
                break;
        }
    /*    if (Dungeon.hero.isAlive() && Dungeon.depth != 22) {
            Badges.validateNoKilling();
        }*/
    }

    private static void sendMessage(boolean acend){
        if (acend) {
            if (Dungeon.depth < Statistics.deepestFloor) {
                GLog.h( TXT_WELCOME_BACK, Dungeon.depth );
            } else {
                GLog.h( TXT_WELCOME, Dungeon.depth );
                Sample.INSTANCE.play( Assets.SND_DESCEND );
            }
            switch (Dungeon.level.feeling) {
                case CHASM:
                    GLog.w( TXT_CHASM );
                    break;
                case WATER:
                    GLog.w( TXT_WATER );
                    break;
                case GRASS:
                    GLog.w( TXT_GRASS );
                    break;
                default:
            }
            if (Dungeon.level instanceof RegularLevel &&
                    ((RegularLevel) Dungeon.level).secretDoors > Random.IntRange( 3, 4 )) {
                GLog.w( TXT_SECRETS );
            }
            if (Dungeon.nightMode && !Dungeon.bossLevel(Dungeon.depth)) {
                GLog.w( TXT_NIGHT_MODE );
            }

        }
    }

    public static void descend(@Nullable Hero hero)  {// спуск

        try {
            Generator.reset();
            for (int i = 0; i < heroes.length; i++) {
                SendData.sendInterLevelScene(i,"DESCEND");
            }
            Actor.fixTime();
            if (Dungeon.depth > 0) {
                Dungeon.saveLevel();
            }

            Level level;
            level = getNextLevel();
            if (hero == null) {
                Dungeon.switchLevel(level);
            } else {
                Dungeon.switchLevel(level, level.entrance, hero);
            }
            for (int i = 0; i < heroes.length; i++) {
                SendData.sendInterLevelSceneFadeOut(i);
            }
            ShowStoryIfNeed(Dungeon.depth);
            sendMessage(false);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public static  void  fall(Hero  hero){
     fall(hero,false);
    }
    public static void fall(Hero hero, boolean fallIntoPit) {

        try {
            Generator.reset();
            for (int i = 0; i < heroes.length; i++) {
                SendData.sendInterLevelScene(i, "FALL");
            }
            Actor.fixTime();
            Dungeon.saveLevel();

            Level level;
            level = getNextLevel();
            Dungeon.switchLevel(level, fallIntoPit ? level.pitCell() : level.randomRespawnCell(), hero);

            for (int i = 0; i < heroes.length; i++) {
                SendData.sendInterLevelSceneFadeOut(i);
            }
            for (Hero hero_ : heroes) {
                if (hero_ != null && hero.isAlive()) {
                    Chasm.heroLand(hero_);
                }
            }

            ShowStoryIfNeed(Dungeon.depth);
            sendMessage(false);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    private static Level getNextLevel()throws IOException {

        if (Dungeon.depth >= Statistics.deepestFloor) {
            return  Dungeon.newLevel();
        } else {
            Dungeon.depth++;
            return Dungeon.loadLevel();
        }
    };

    public static void ascend(Hero hero) {

        try {
            Generator.reset();
        for (int i = 0; i < heroes.length; i++) {
            SendData.sendInterLevelScene(i,"ASCEND");
        }
        Actor.fixTime();

            Dungeon.saveLevel();
            Dungeon.depth--;
            Level level = Dungeon.loadLevel();
            Dungeon.switchLevel(level, level.exit, hero);

            for (int i = 0; i < heroes.length; i++) {
                SendData.sendInterLevelSceneFadeOut(i);
            }
            sendMessage(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void returnTo(int  depth, int pos, Hero  hero) {
        try {
            Generator.reset();
            if (depth != Dungeon.depth) {
                for (int i = 0; i < heroes.length; i++) {
                    SendData.sendInterLevelScene(i,"RETURN");
                }

                Actor.fixTime();
                Dungeon.saveLevel();
                Dungeon.depth = depth;
                Level level = Dungeon.loadLevel();
                Dungeon.switchLevel(level, pos, hero);
                for (int i = 0; i < heroes.length; i++) {
                    SendData.sendInterLevelSceneFadeOut(i);
                    sendMessage(true);
                }
            } else {
                hero.pos = getNearClearCell(pos);
            }
            WandOfBlink.appear(hero, hero.pos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void restore() { //when loading from save

        try {
            Generator.reset();
            Actor.fixTime();

            GameLog.wipe();

            Dungeon.loadGame(StartScene.curClass);
            if (Dungeon.depth == -1) {
                Dungeon.depth = Statistics.deepestFloor;
                Dungeon.switchLevel(Dungeon.loadLevel(StartScene.curClass));
            } else {
                Level level = Dungeon.loadLevel(StartScene.curClass);
                Dungeon.switchLevel(level);
            }
        }catch (IOException  e){
            throw new RuntimeException(e);
        }
    }

    public static void resurrect(Hero hero)  { //respawn by ankh

        Generator.reset();
        for (int i = 0; i< heroes.length; i++) {
            SendData.sendInterLevelScene(i, "RESURRECT");
        }
        Actor.fixTime();
        switch (Settings.resurrectMode){
            case RESET_LEVEL: {
                if (Dungeon.bossLevel(Dungeon.depth)) {
                    hero.resurrect(Dungeon.depth);
                    Dungeon.depth--;
                    Level level = Dungeon.newLevel();
                    Dungeon.switchLevelToAll(level, level.entrance);
                } else {
                    hero.resurrect(-1);
                    Dungeon.resetLevel();
                }
            }
            case RESPAWN_HERO:
            {
                Dungeon.switchLevel(Dungeon.level,Dungeon.level.entrance, hero);
            }
        }
        WandOfBlink.appear(hero,hero.pos);
        for (int i = 0; i< heroes.length; i++) {
            SendData.sendInterLevelSceneFadeOut(i);
        }

        sendMessage(false);
    }

}

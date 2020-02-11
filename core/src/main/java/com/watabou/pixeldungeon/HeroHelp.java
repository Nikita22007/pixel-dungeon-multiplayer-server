package com.watabou.pixeldungeon;

import com.watabou.pixeldungeon.actors.hero.Hero;

import java.util.Arrays;

public class HeroHelp {
    public static int HeroCount(){
        int count=0;
        for (int i=0;i<Settings.maxPlayers;i++){
            if (!(Dungeon.heroes[i]==null)){
                count++;
            }
        }
        return  count;
    }
    public static int HeroesCountOnLevel(int depth){
        return HeroCount();
        /*
        int count=0;
        for (int i=0;i<Settings.maxPlayers;i++){
            if (!(Dungeon.heroes[i]==null)){
            if (Dungeon.heroes[i].depth==depth){
                count++;
                }
            }
        }
        return  count;
         */
    }
    public static Hero GetHeroOnLevel(int depth){ //use  this  if on level  only  one Hero
        return Dungeon.heroes[0];//fixme  This need to be other code
        // Dungeon.heroes[i]==depth?  return  Dungeon.heroes[i];
    };
    public static int getHeroID(Hero hero){
        return Arrays.asList(Dungeon.heroes).indexOf(hero);
    }
    public static boolean haveAliveHero(){
        for (int i=0;i<Settings.maxPlayers;i++){
             if (Dungeon.heroes[i].isAlive()){
                 return true;
             }
        }
        return false;
        }
}

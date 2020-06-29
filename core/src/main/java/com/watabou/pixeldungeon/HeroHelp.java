package com.watabou.pixeldungeon;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static com.watabou.pixeldungeon.Dungeon.heroes;

public class HeroHelp {
    public static int HeroCount(){
        int count=0;
        for (int i=0;i<Settings.maxPlayers;i++){
            if ((!(heroes[i]==null))&&(heroes[i].isAlive())){
                count++;
            }
        }
        return  count;
    }
    public static Hero GetRandomHero()
    {
        HashSet<Hero> HeroesList = new HashSet<>();
        for (int i = 0; i< heroes.length; i++)
            if (heroes[i]!=null && heroes[i].isAlive())
            {
                HeroesList.add(heroes[i]);
            }

        return HeroesList.size()==0? null:  com.watabou.utils.Random.element(HeroesList);
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

    public static String GetHeroesClass()
    {
        int  count =  HeroCount();
        if  (count==1)
        {
            for (int i=0; i<Settings.maxPlayers; i++)
            {
                if ((!(heroes[i]==null))&&(heroes[i].isAlive())){
                    return heroes[i].className();
                }
            }
        }
        String ClassName="";
        if (count>1)
        {

            for (int i=0; i<Settings.maxPlayers; i++)
            {
                if ((!(heroes[i]==null))&&(heroes[i].isAlive())){
                    if (ClassName=="")
                    {
                        ClassName=  heroes[i].className();
                    }
                    else
                    {
                    if (ClassName!= heroes[i].className());
                        {
                            return "heroes";
                        }
                    }
                }
            }
            return ClassName+'s';
        }
        return "ERROR";
    }
    public static Hero GetHeroOnLevel(int depth){ //use  this  if on level  only  one Hero
        return heroes[0];//fixme  This need to be other code
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

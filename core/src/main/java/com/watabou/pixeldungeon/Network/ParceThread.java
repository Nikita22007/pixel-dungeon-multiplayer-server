package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.CustomMob;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.IOException;

import static com.watabou.pixeldungeon.network.Client.readStream;
import static com.watabou.pixeldungeon.network.Client.socket;
import static com.watabou.pixeldungeon.network.Codes.*;
public class ParceThread extends Thread {

    @Override
    public void run() {
        while (!socket.isClosed()){
            try {
                int code = (Integer) readStream.readInt();
                switch (code){
                    //Network block
                    case Codes.NOP: {break;}
                    case Codes.SERVER_FULL:{
                        PixelDungeon.switchScene(TitleScene.class);
                        // TODO   PixelDungeon.scene().add(new WndError("Server full"));
                        return;
                       }
                    //level block
                    case LEVEL_MAP: {
                        Dungeon.level.map= readIntArray();break;
                    }
                    case LEVEL_VISITED: {
                        Dungeon.level.visited= readBooleanArray();break;
                    }
                    case LEVEL_MAPPED:{
                        Dungeon.level.mapped = readBooleanArray();break;
                    }
                    case  Codes.LEVEL_ENTRANCE:{
                        Dungeon.level.entrance  = readStream.readInt();break;
                    }
                    case  Codes.LEVEL_EXIT:{
                        Dungeon.level.exit  = readStream.readInt();break;
                    }
                    //UI block
                    case Codes.IL_FADE_OUT: {
                        InterlevelScene.phase  = InterlevelScene.Phase.FADE_OUT;break;
                    }
                    //Hero block
                    case Codes.HERO_VISIBLE_AREA:{
                        Dungeon.visible=readBooleanArray();  break;
                    }
                    case HERO_STRENGTH:{
                        Dungeon.hero.STR =  readStream.readInt();break;
                    }
                    case Codes.HERO_ACTOR_ID:{
                        Dungeon.hero.changeID(readStream.readInt()); break;
                    }
                    //Char block
                    case CHAR:{ //all Heroes (that is not current player hero) are  nobs
                        int ID =  readStream.readInt();
                        //boolean erase_old =  readStream.readBoolean();
                        boolean erase_old  = false;
                        if (erase_old || Actor.findById(ID)==null) {
                            Mob chr = new CustomMob(ID);
                            GameScene.add_without_adding_sprite(chr);
                        }
                        break;
                    }
                    case CHAR_POS:{
                        int ID =  readStream.readInt();
                        int pos =  readStream.readInt();
                        Char chr =(Char)Actor.findById(ID);
                        chr.pos=pos;
                        break;
                    }
                    case CHAR_HT:{
                        int ID =  readStream.readInt();
                        int HT =  readStream.readInt();
                        Char chr =(Char)Actor.findById(ID);
                        chr.HT=HT;
                        break;
                    }
                    case CHAR_HP:{
                        int ID =  readStream.readInt();
                        int HP =  readStream.readInt();
                        Char chr =(Char)Actor.findById(ID);
                        chr.HP=HP;
                        break;
                    }
                    case CHAR_NAME:{
                        int ID =  readStream.readInt();
                        String  name = readString();
                        Char chr =(Char)Actor.findById(ID);
                        chr.name=name;
                        break;
                    }
                    default:{
                        GLog.h("Bad  code: {0}",code);
                    }
                }
            }catch (IOException e){
                GLog.n(e.getMessage());

                PixelDungeon.switchScene(TitleScene.class);
//                PixelDungeon.scene().add(new WndError("Disconnected"));
                return;
            }
        }
    }
    protected int[] readIntArray()throws IOException{
        int len =  readStream.readInt();
        int[] res =new int[len];
        for  (int i=0;i<len;i++){
            res[i]=readStream.readInt();
        }
        return res;
    }
    protected String readString()throws IOException{
        int len =  readStream.readInt();
        char[] chars =new char[len];
        for  (int i=0;i<len;i++){
            chars[i]=readStream.readChar();
        }
        return new String(chars);
    }
    protected boolean[] readBooleanArray()throws IOException{
        int len =  readStream.readInt();
        boolean[] res =new boolean[len];
        for  (int i=0;i<len;i++){
            res[i]=readStream.readBoolean();
        }
        return res;
    }
}

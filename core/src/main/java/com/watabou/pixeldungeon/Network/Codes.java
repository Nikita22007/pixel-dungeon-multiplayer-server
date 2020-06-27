package com.watabou.pixeldungeon.Network;

/*
Нужно мерджить эти коды в ветки клиента и сервера после каждого изменения.
Эти числа  не должны изменяться без важных на то причин.
Числа  указывают на  код операции, выполняемым данным пакетом.
Прогон через  switch  case
*/
class Codes {
    //Network block 0x00-0x0F
    public static final int NOP           = 0X00;   //nothing
    public static final int SERVER_FULL   = 0X01;   //server->client;
    public static final int SERVER_CLOSED = 0X02;   //server->client; when server is going off

    //level block 0X10-0X1F
    public static final int LEVEL_MAP     = 0X10;   //client->server:ask; //server->client:answer or  data
    public static final int LEVEL_VISITED = 0X11;   //client->server:ask; //server->client:answer or  data
    public static final int LEVEL_MAPPED  = 0X12;   //client->server:ask; //server->client:answer or  data

    //UI block 0X20-0X3F
    public static final int BOSS_SLAIN    = 0X20;   //server->client

    public static final int IL_DESCEND    = 0x21;   //server->client //IL=INTERLEVEL
    public static final int IL_FALL       = 0x22;   //server->client
    public static final int IL_ASCEND     = 0x23;   //server->client
    public static final int IL_RETURN     = 0x24;   //server->client
    public static final int IL_RESURRECT  = 0x25;   //server->client
    public static final int IL_OTHER      = 0x26;   //server->client //text:string
    public static final int IL_FADE_OUT   = 0x26;   //server->client //when IL loading finished

    //Hero block 0x40-0X5F
    public static final int HERO_CLASS    = 0x40;   //0 = random;  1 = warrior; 2=mage; 3=rouge; 4=huntress; other=error+random //client->server
    public static final int HERO_SUBCLASS = 0x41;   //???

    public static final int HERO_STRENGTH = 0x42;   //client->server:ask; //server->client; when hero generated or update strength
    public static final int HERO_HP       = 0x43;   //client->server:ask; //server->client:answer or data
    public static final int HERO_HT       = 0x44;   //client->server:ask; //server->client:answer or data

    public static final int HERO_VISIBLE_AREA  = 0x50;   //client->server:ask; //server->client: answer or dara;//Hero's field of view


    //Control block 0x60-0x6F
    public static final int CELL_SELECT   = 0x60;   //client->server; when client touch cell

    //Badges block 0x70-0x8F  //server->client; param(if need)  is level of bage
    public static final int BADGE_MONSTERS_SLAIN      = 0x70;     //param need
    public static final int BADGE_GOLD_COLLECTED      = 0x71;     //param need
    public static final int BADGE_BOSS_SLAIN          = 0x72;     //param need
    public static final int BADGE_LEVEL_REACHED       = 0x73;     //param need
    public static final int BADGE_STRENGTH_ATTAINED   = 0x74;     //param need
    public static final int BADGE_FOOD_EATEN          = 0x75;     //param need
    public static final int BADGE_ITEM_LEVEL          = 0x76;     //param need
    public static final int BADGE_POTIONS_COOKED      = 0x77;     //param need
    public static final int BADGE_DEATH_FROM_FIRE     = 0x78;
    public static final int BADGE_DEATH_FROM_GAS      = 0x79;
    public static final int BADGE_DEATH_FROM_HUNGER   = 0x7A;
    public static final int BADGE_DEATH_FROM_POISON   = 0x7B;
    public static final int BADGE_ALL_POTIONS_IDENTIFIED  = 0x7C;
    public static final int BADGE_ALL_SCROLLS_IDENTIFIED  = 0x7D;
    public static final int BADGE_ALL_RINGS_IDENTIFIED    = 0x7E;
    public static final int BADGE_ALL_WANDS_IDENTIFIED    = 0x7F;
    public static final int BADGE_VICTORY                 = 0x80;
    public static final int BADGE_MASTERY            = 0x81;


    //unclassed

    public static final int ITEM_BROKEN   =0xE0;   //server->client;
}

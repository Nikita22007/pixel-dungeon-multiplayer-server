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

    //Hero block 0x40-0X5F
    public static final int HERO_CLASS    = 0x40;   //1 = warrior; 2=mage; 3=rouge; 4=huntress; other=random //client->server
    public static final int HERO_STRENGTH = 0x41;   //server->client; when hero generated or  update strength
    public static final int HERO_SUBCLASS = 0x42;   //???

    public static final int HERO_VISIBLE_AREA  = 0x43;   //client->server:ask; //server->client: answer or dara;//Hero's field of view


    //Control block 0x60-0x6F
    public static final int CELL_SELECT   = 0x60;   //client->server; when client touch cell

}

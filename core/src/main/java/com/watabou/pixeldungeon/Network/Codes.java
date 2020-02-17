package com.watabou.pixeldungeon.Network;

/*
Нужно мерджить эти коды в ветки клиента и сервера после каждого изменения.
Эти числа  не должны изменяться без важных на то причин.
Числа  указывают на  код операции, выполняемым данным пакетом.
Прогон через  switch  case
*/
class Codes {
    //Network block 0x00-0x0F
    public static final int NOP = 0X00;
    public static final int SERVER_FULL = 0X01;
    public static final int SERVER_CLOSED = 0X02;

    //level block 0X10-0X1F
    public static final int LEVEL_MAP = 0X10;
    public static final int LEVEL_VISITED = 0X11;
    public static final int LEVEL_MAPPED = 0X12;
}

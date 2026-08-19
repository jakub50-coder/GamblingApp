//What the class does is get each emoji value that is stored in this class.
package com.gambingapp.gaminghub.model.slots;

public enum SlotSymbol{
    CHERRY("🍒",1),
    LEMON("🍋",2),
    GRAPES("🍇",3),
    STAR("⭐",4),
    BELL("🔔",5),
    DIAMOND("💎",6),
    SEVEN("7️⃣",7);

    private final String emoji;
    private final int value;

    SlotSymbol(String emoji, int value){
        this.emoji  = emoji;
        this.value = value;
    }
    public String getEmoji(){
        return emoji;
    }
    public int getValue(){
        return value;
    }
}

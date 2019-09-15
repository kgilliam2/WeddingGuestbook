package app;

import java.util.ArrayList;

enum PenStates {
    PEN_UP, PEN_DOWN;
}

class GRBL_Setting_Strings {

    public ArrayList<String> programStrings = new ArrayList<>();
    private final int X_MAX_RATE = 12000;
    private final int Y_MAX_RATE = 12000;
    private final int Z_MAX_RATE = 12000;

    GRBL_Setting_Strings() {
        programStrings.add("$0=10");
        programStrings.add("$1=255");
        programStrings.add("$2=0");
        programStrings.add("$3=2");
        programStrings.add("$4=1");
        programStrings.add("$5=0");
        programStrings.add("$6=0");
        programStrings.add("$10=1");
        programStrings.add("$11=0.050");
        programStrings.add("$12=0.002");
        programStrings.add("$13=0");
        programStrings.add("$20=1");
        programStrings.add("$21=1");
        programStrings.add("$22=1");
        programStrings.add("$23=3");
        programStrings.add("$24=300.000");
        programStrings.add("$25=5000.000");
        programStrings.add("$26=30");
        programStrings.add("$27=5.000");
        programStrings.add("$30=1000");
        programStrings.add("$31=0");
        programStrings.add("$32=0");
        programStrings.add("$100=57.143");
        programStrings.add("$101=57.143");
        programStrings.add("$102=1.000");
        programStrings.add("$110=" + X_MAX_RATE);
        programStrings.add("$111=" + Y_MAX_RATE);
        programStrings.add("$112=" + Z_MAX_RATE);
        programStrings.add("$120=1000.000");
        programStrings.add("$121=1000.000");
        programStrings.add("$122=800.000");
        programStrings.add("$130=800.000");
        programStrings.add("$131=340.000");
        programStrings.add("$132=70.000");

    }

    int size(){
        return programStrings.size();
    }
    String at(int idx){
        return programStrings.get(idx);
    }
}
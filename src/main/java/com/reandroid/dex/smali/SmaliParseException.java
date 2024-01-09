package com.reandroid.dex.smali;

import java.io.IOException;

public class SmaliParseException extends IOException {

    private final SmaliReader reader;

    public SmaliParseException(String message, SmaliReader reader){
        super(message);
        this.reader = reader;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        SmaliReader reader = this.reader;
        if(reader == null){
            return message;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(message);
        String path = reader.getPath();
        String label = reader.getPositionLabel();
        if(path != null){
            builder.append("\n");
            builder.append(path);
            builder.append(" ");
            builder.append(label);
        }else {
            builder.append(", at ");
            builder.append(label);
        }
        builder.append("\n");
        builder.append(reader.getPositionPointer());
        return builder.toString();
    }

    public static void expect(SmaliReader reader, SmaliDirective expect) throws IOException{
        expect(reader, expect, false);
    }
    public static void expect(SmaliReader reader, SmaliDirective expect, boolean end) throws IOException{
        int position = reader.position();
        boolean error = false;
        if(end){
            if(!expect.isEnd(reader)){
                error = true;
            }
        }
        if(!error){
            if(SmaliDirective.parse(reader) != expect){
                error = true;
            }
        }
        if(!error){
            return;
        }
        reader.position(position);
        String message = "expecting '" + expect.toString(end) + "'";
        SmaliParseException exception = new SmaliParseException(message, reader);
        sanitizeStackTrace(exception);
        throw exception;
    }
    public static void expect(SmaliReader reader, char ch) throws IOException{
        if(reader.readASCII() == ch){
            return;
        }
        reader.skip(-1);
        String message = "expecting '" + ch + "'";
        SmaliParseException exception = new SmaliParseException(message, reader);
        sanitizeStackTrace(exception);
        throw exception;
    }
    public static char expect(SmaliReader reader, char ch1, char ch2) throws IOException{
        char ch = reader.readASCII();
        if(ch == ch1 || ch == ch2){
            return ch;
        }
        reader.skip(-1);
        String message = "expecting '" + ch1 + "', or '" + ch2 + "'";
        SmaliParseException exception = new SmaliParseException(message, reader);
        sanitizeStackTrace(exception);
        throw exception;
    }
    private static void sanitizeStackTrace(Exception exception){
        StackTraceElement[] elements = exception.getStackTrace();
        int count = 0;
        for(int i = 0; i < elements.length; i++){
            StackTraceElement element = elements[i];
            if(remove(element)){
                elements[i] = null;
            }else {
                count ++;
            }
        }
        StackTraceElement[] result = new StackTraceElement[count];
        count = 0;
        for(StackTraceElement element : elements){
            if(element != null){
                result[count] = element;
                count ++;
            }
        }
        exception.setStackTrace(result);
    }
    private static boolean remove(StackTraceElement element){
        if(element == null){
            return true;
        }
        String name = element.getClassName();
        return name.equals(SmaliParseException.class.getName());
    }
}

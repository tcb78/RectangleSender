package com.example.taichiabe.rectanglesender;

public class SoundDto {

    //音声データ
    private byte[] sound;
    //長さ
    private double length;

    /**
     * 引数付きコンストラクタ
     * @param source 音声データ
     * @param length 長さ
     */
    public SoundDto(byte[] source, double length) {
        this.sound = source;
        this.length = length;
    }

    public SoundDto(byte[] source) {
        this.sound = source;
    }

    public byte[] getSound() {
        return sound;
    }
    public void setSound(byte[] sound) {
        this.sound = sound;
    }
    public double getLength() {
        return length;
    }
    public void setLength(double length) {
        this.length = length;
    }
}

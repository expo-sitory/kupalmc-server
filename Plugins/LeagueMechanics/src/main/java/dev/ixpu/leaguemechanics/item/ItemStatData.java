package dev.ixpu.leaguemechanics.item;

public class ItemStatData {
    private String id;
    private String name;

    private double ad;
    private double ap;
    private double ar;
    private double mr;

    private double hp;
    private double hr;
    private double sr;
    private double as;
    private double ms;

    public ItemStatData(String id, String name, double ad, double ap, double ar, double mr, double hp, double hr, double sr, double as, double ms) {
        this.id = id;
        this.name = name;
        this.ad = ad;
        this.ap = ap;
        this.ar = ar;
        this.mr = mr;
        this.hp = hp;
        this.hr = hr;
        this.sr = sr;
        this.as = as;
        this.ms = ms;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAd() {
        return ad;
    }

    public void setAd(double ad) {
        this.ad = ad;
    }

    public double getAp() {
        return ap;
    }

    public void setAp(double ap) {
        this.ap = ap;
    }

    public double getAr() {
        return ar;
    }

    public void setAr(double ar) {
        this.ar = ar;
    }

    public double getMr() {
        return mr;
    }

    public void setMr(double mr) {
        this.mr = mr;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public double getHr() {
        return hr;
    }

    public void setHr(double hr) {
        this.hr = hr;
    }

    public double getSr() {
        return sr;
    }

    public void setSr(double sr) {
        this.sr = sr;
    }

    public double getAs() {
        return as;
    }

    public void setAs(double as) {
        this.as = as;
    }

    public double getMs() {
        return ms;
    }

    public void setMs(double ms) {
        this.ms = ms;
    }
}
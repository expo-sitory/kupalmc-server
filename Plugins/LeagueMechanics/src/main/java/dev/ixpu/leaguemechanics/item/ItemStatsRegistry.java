package dev.ixpu.leaguemechanics.item;

public class ItemStatsRegistry {
    private String id;
    private String name;

    private double hp;
    private double hr;

    private double ad;
    private double ap;

    private double td;
    private double as;
    private double ar;
    private double mr;
    private double ls;
    private double cc;
    private double sr;

    private double ms;
    private boolean hasPassive;
    private String passiveId;

    public ItemStatsRegistry(String id, String name,
                             double hp, double hr, double ad, double ap, double td, double as,
                             double ar, double mr, double ls, double cc, double sr, double ms) {
        this(id, name, hp, hr, ad, ap, td, as, ar, mr, ls, cc, sr, ms, false, null);
    }

    public ItemStatsRegistry(String id, String name,
                             double hp, double hr, double ad, double ap, double td, double as,
                             double ar, double mr, double ls, double cc, double sr, double ms,
                             boolean hasPassive, String passiveId) {
        this.id = id;
        this.name = name;

        this.hp = hp;
        this.hr = hr;

        this.ad = ad;
        this.ap = ap;

        this.td = td;
        this.as = as;
        this.ar = ar;
        this.mr = mr;
        this.ls = ls;
        this.cc = cc;
        this.sr = sr;

        this.ms = ms;
        this.hasPassive = hasPassive;
        this.passiveId = passiveId;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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


    public double getTd() {
        return td;
    }
    public void setTd(double td) {
        this.td = td;
    }

    public double getAs() {
        return as;
    }
    public void setAs(double as) {
        this.as = as;
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

    public double getLs() {
        return ls;
    }
    public void setLs(double ls) {
        this.ls = ls;
    }

    public double getCc() {
        return cc;
    }
    public void setCc(double cc) {
        this.cc = cc;
    }

    public double getSr() {
        return sr;
    }
    public void setSr(double sr) {
        this.sr = sr;
    }

    public double getMs() {
        return ms;
    }
    public void setMs(double ms) {
        this.ms = ms;
    }

    public boolean hasPassive() {
        return hasPassive;
    }

    public String getPassiveId() {
        return passiveId;
    }
}
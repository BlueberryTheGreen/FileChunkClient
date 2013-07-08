package com.agiac.filechunk.peer;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Adam
 * Date: 7/7/13
 * Time: 9:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReputationSet {
    private final List<Peer> highRep = new ArrayList<Peer>();
    private final List<Peer> medRep = new ArrayList<Peer>();
    private final List<Peer> lowRep = new ArrayList<Peer>();
    private final List<Peer> noRep = new ArrayList<Peer>();

    private final Random r = new Random();

    public enum Reputation {
        NO,
        LOW,
        MEDIUM,
        HIGH;
    }

    public void changeGrade(Peer peer, Reputation reputation) {
        if(noRep.contains(peer)) {
            noRep.remove(peer);
        }
        if(lowRep.contains(peer)) {
            lowRep.remove(peer);
        }
        if(medRep.contains(peer)) {
            medRep.remove(peer);
        }
        if(highRep.contains(peer)) {
            highRep.remove(peer);
        }
        switch(reputation) {
            case NO:
                noRep.add(peer);
                break;
            case LOW:
                lowRep.add(peer);
                break;
            case MEDIUM:
                medRep.add(peer);
                break;
            case HIGH:
                highRep.add(peer);
                break;
            default:
                break;
        }
    }

    public void downgrade(Peer peer) {
        if (highRep.contains(peer)) {
            highRep.remove(peer);
            medRep.add(peer);
            System.out.println("Reducing reputation from High to Medium");
        } else if (medRep.contains(peer)) {
            medRep.remove(peer);
            lowRep.add(peer);
            System.out.println("Reducing reputation from Medium to Low");
        } else if (lowRep.contains(peer)) {
            lowRep.remove(peer);
            noRep.add(peer);
            System.out.println("Reducing reputation from Low to None");
        } else {
            // Keep the reputation as None
        }
    }

    public void upgrade(Peer peer) {
        if (noRep.contains(peer)) {
            noRep.remove(peer);
            lowRep.add(peer);
            System.out.println("Reducing reputation from No to Low");
        } else if (medRep.contains(peer)) {
            medRep.remove(peer);
            highRep.add(peer);
            System.out.println("Reducing reputation from Medium to High");
        } else if (lowRep.contains(peer)) {
            lowRep.remove(peer);
            medRep.add(peer);
            System.out.println("Reducing reputation from Low to Medium");
        }
    }

    public boolean hasPeerWithReputation() {
        return !(highRep.isEmpty() && medRep.isEmpty() && lowRep.isEmpty());
    }

    public Peer getRandomHighestRepPeer() {
        List<Peer> bestRep = !highRep.isEmpty()?highRep:!medRep.isEmpty()?medRep:!lowRep.isEmpty()?lowRep:null;
        if(bestRep != null) {
            r.setSeed(System.nanoTime());
            int random = r.nextInt() % bestRep.size();
            if (random < 0) {
                random *= -1;
            }
            return bestRep.get(random);
        }
        return null;
    }

    public Peer populateRepSetAndGetRandomIp(List<Peer> ips) {
        r.setSeed(System.nanoTime());
        int random = r.nextInt() % ips.size();
        if (random < 0) {
            random *= -1;
        }
        Peer peer = ips.get(random);
        r.setSeed(System.nanoTime());
        int count = 0;
        while(noRep.contains(peer)) {
            random = r.nextInt(Integer.MAX_VALUE) % ips.size();
            peer = ips.get(random);
            count = count + 1;
        }

        medRep.add(peer);
        System.out.println("Default assignment: Medium reputation");
        return  medRep.get(0);
    }
}

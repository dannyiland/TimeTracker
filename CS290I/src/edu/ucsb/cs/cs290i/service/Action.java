package edu.ucsb.cs.cs290i.service;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ucsb.cs.cs290i.service.detectors.Detector;
import edu.ucsb.cs.cs290i.service.detectors.Event;

public class Action implements Externalizable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Detector> detectors;


    public Action() {
        detectors = new ArrayList<Detector>();
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void addDetector(Detector d) {
        detectors.add(d);
    }


    public boolean matches(List<Event> events) {
        nextDetector: for (Detector d : detectors) {
            for (Event e : events) {
                if (e.getDetector().equals(d)) {
                    // Events contains the detector, try the next one.
                    continue nextDetector;
                }
            }
            // Events does not contain the required detector, Action does not match.
            return false;
        }

        return true;
    }


    public List<Detector> getDetectors() {
        return detectors;
    }


    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        name = input.readUTF();
        for (int i = input.readInt(); i > 0; i--) {
            String[] params = (String[]) input.readObject();
            @SuppressWarnings("unchecked")
            Class<? extends Detector> c = (Class<? extends Detector>) input.readObject();
            try {
                Detector detector = (Detector) c.getConstructor(params.getClass()).newInstance((Object) params);
                System.out.printf("Created detector %s(%s)\n", c.getName(), Arrays.toString(params));
                detectors.add(detector);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeUTF(name);
        output.writeInt(detectors.size());
        for (Detector d : detectors) {
            output.writeObject(d.getParameters());
            output.writeObject(d.getClass());
        }
    }
    
    @Override
    public String toString() {
        return getName();
    }

}

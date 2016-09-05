package com.contribly.reference.android.example.activities.views;

import io.swagger.client.model.Artifact;
import io.swagger.client.model.Contribution;
import io.swagger.client.model.MediaUsage;

public class ArtifactFinder {

    public static Artifact findMainArtifact(Contribution contribution) {
        for(MediaUsage mediaUsage: contribution.getMediaUsages()) {
            Artifact a = findArtifact(mediaUsage);
            if (a != null ) {
                return a;
            }
        }
        return null;
    }

    public static Artifact findArtifact(MediaUsage mediaUsage) {
        for (Artifact artifact : mediaUsage.getArtifacts()) {
            if (artifact.getLabel().equals("mediumdouble") && artifact.getUrl() != null) {
                return artifact;
            }
        }
        return null;
    }

}

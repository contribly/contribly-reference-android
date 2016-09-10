package com.contribly.reference.android.example.model;

import java.util.List;

import com.contribly.client.model.Contribution;

public class ResultSet {

    private long numberOfResults;
    private List<Contribution> contributions;

    public ResultSet(long numberOfResults, List<Contribution> contributions) {
        this.numberOfResults = numberOfResults;
        this.contributions = contributions;
    }

    public long getNumberOfResults() {
        return numberOfResults;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }

    @Override
    public String toString() {
        return "ResultSet{" +
                "numberOfResults=" + numberOfResults +
                ", contributions=" + contributions +
                '}';
    }

}

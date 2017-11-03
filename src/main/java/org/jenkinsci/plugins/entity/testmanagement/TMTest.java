package org.jenkinsci.plugins.entity.testmanagement;

public class TMTest {

    private int id;
    private String status;
    private String steps;
    private String expectedResult;

    public TMTest(int id, String status, String steps, String expectedResult) {
        this.id = id;
        this.status = status;
        this.steps = steps;
        this.expectedResult = expectedResult;
    }

    public TMTest() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    @Override
    public String toString() {
        return "TMTest{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", steps='" + steps + '\'' +
                ", expectedResult='" + expectedResult + '\'' +
                '}';
    }
}

package org.jenkinsci.plugins.entity;

/**
 * The entity for Test Case entity (Test Management plugin)
 */
public class Test {

    private int id;
    private String status;
    private String steps;
    private String expectedResult;

    public Test(int id, String status, String steps, String expectedResult) {
        this.id = id;
        this.status = status;
        this.steps = steps;
        this.expectedResult = expectedResult;
    }

    public Test() {
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
        return "Test{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", steps='" + steps + '\'' +
                ", expectedResult='" + expectedResult + '\'' +
                '}';
    }
}

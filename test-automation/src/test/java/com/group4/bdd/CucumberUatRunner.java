package com.group4.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.group4.bdd.steps", "com.group4.bdd.hooks"},
    plugin = {"pretty", "json:target/cucumber-uat.json"},
    tags = "@uat and not @root-login and not @deferred")
public class CucumberUatRunner {}

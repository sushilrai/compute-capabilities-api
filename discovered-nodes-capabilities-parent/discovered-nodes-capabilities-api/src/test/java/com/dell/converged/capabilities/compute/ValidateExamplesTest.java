/**
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 */

package com.dell.converged.capabilities.compute;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.dell.cpsd.common.json.utils.JsonSchemaValidation.validateSchema;
import static org.junit.Assert.assertNull;

/**
 * This is the schema validation test, this validates json messages
 * conforms to the API.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@RunWith(Parameterized.class)
public class ValidateExamplesTest
{
    public static final String SCHEMA_DIR   = "/discovered-nodes-capabilities/schema/json/";
    public static final String EXAMPLE_DIR  = "/discovered-nodes-capabilities/schema/example/";
    public static final String INCLUDES_DIR = SCHEMA_DIR + "includes/";

    @Parameterized.Parameters(name = "{index}: {0} model test")
    public static List<String> getExamples() throws URISyntaxException {
        File resources = new File(ValidateExamplesTest.class.getResource(EXAMPLE_DIR).toURI());
        return Arrays.stream(resources.list()).filter(file -> file.endsWith(".json")).collect(Collectors.toList());
    }
    @Parameterized.Parameter
    public String name;

    @Test
    public void exampleModelTest() throws Exception
    {
        String errors = validateSchema(SCHEMA_DIR + name.replace(".json", ".jsd"), EXAMPLE_DIR + name, INCLUDES_DIR);
        assertNull(errors, errors);
    }
}


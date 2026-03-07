# JSON Schema

This document describes the structure of classes used by the WebSocket API.

## Requests

These classes are used when sending requests to the server.

### Class Request

Structure of the Request class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "invocationId" : {
      "type" : "integer"
    },
    "method" : {
      "type" : "string"
    },
    "methodVersion" : {
      "type" : "integer"
    },
    "params" : {
      "type" : "object"
    }
  }
}
```

### Class UpdateSelectedProcessorParams

Structure of the UpdateSelectedProcessorParams class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "code" : {
      "type" : "string"
    }
  }
}
```

### Class UpdateProcessorsOnMapParams

Structure of the UpdateProcessorsOnMapParams class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "code" : {
      "type" : "string"
    },
    "programId" : {
      "type" : "object",
      "properties" : {
        "idPrefix" : {
          "type" : "string"
        },
        "major" : {
          "type" : "integer"
        },
        "minor" : {
          "type" : "integer"
        },
        "revision" : {
          "type" : "integer"
        }
      }
    },
    "variableName" : {
      "type" : "string"
    },
    "versionSelection" : {
      "type" : "string"
    }
  }
}
```

### Class PutSchematicInLibraryParams

Structure of the PutSchematicInLibraryParams class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "overwrite" : {
      "type" : "boolean"
    },
    "schematic" : {
      "type" : "string"
    }
  }
}
```

## Responses

These classes are used by responses sent back from the server.

### Class Response

Structure of the Response class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "invocationId" : {
      "type" : "integer"
    },
    "result" : {
      "type" : "object"
    },
    "resultType" : {
      "type" : "string"
    },
    "status" : {
      "type" : "string"
    }
  }
}
```

### Class TextResult

Structure of the TextResult class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "text" : {
      "type" : "string"
    }
  }
}
```

### Class ProcessorUpdateResults

Structure of the ProcessorUpdateResults class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "processorUpdates" : {
      "type" : "array",
      "items" : {
        "type" : "object",
        "properties" : {
          "programId" : {
            "type" : "object",
            "properties" : {
              "idPrefix" : {
                "type" : "string"
              },
              "major" : {
                "type" : "integer"
              },
              "minor" : {
                "type" : "integer"
              },
              "revision" : {
                "type" : "integer"
              }
            }
          },
          "status" : {
            "type" : "string"
          },
          "type" : {
            "type" : "string"
          },
          "x" : {
            "type" : "number"
          },
          "y" : {
            "type" : "number"
          }
        }
      }
    }
  }
}
```

### Class ProcessorExtractResults

Structure of the ProcessorExtractResults class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "code" : {
      "type" : "string"
    }
  }
}
```

## Common

These classes hold other objects used by the interface.

### Class LogicProcessor

Structure of the LogicProcessor class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "programId" : {
      "type" : "object",
      "properties" : {
        "idPrefix" : {
          "type" : "string"
        },
        "major" : {
          "type" : "integer"
        },
        "minor" : {
          "type" : "integer"
        },
        "revision" : {
          "type" : "integer"
        }
      }
    },
    "status" : {
      "type" : "string"
    },
    "type" : {
      "type" : "string"
    },
    "x" : {
      "type" : "number"
    },
    "y" : {
      "type" : "number"
    }
  }
}
```

### Class ProgramId

Structure of the ProgramId class:

```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "idPrefix" : {
      "type" : "string"
    },
    "major" : {
      "type" : "integer"
    },
    "minor" : {
      "type" : "integer"
    },
    "revision" : {
      "type" : "integer"
    }
  }
}
```


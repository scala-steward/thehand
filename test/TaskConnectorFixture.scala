import tasks.TaskConnector

object TaskConnectorFixture {
  def apply(): TaskConnectorFixture = new TaskConnectorFixture()
}

class TaskConnectorFixture extends TaskConnector {
  def assignable(id: Long): String = {
    """{
      |  "ResourceType": "Assignable",
      |  "Id": 1,
      |  "TimeSpent": 1.9,
      |  "Effort": 0,
      |  "EntityType": {
      |    "ResourceType": "EntityType",
      |    "Id": 8,
      |    "Name": "Bug"
      |  },
      |  "Project": {
      |    "ResourceType": "Project",
      |    "Id": 12,
      |    "Name": "Metal RC"
      |  }
      |}""".stripMargin
  }

  def bugs(id: Long): String = {
    """{
      |  "ResourceType": "Bug",
      |  "Id": 1,
      |  "Effort": 0,
      |  "TimeSpent": 1.9,
      |  "EntityType": {
      |    "ResourceType": "EntityType",
      |    "Id": 8,
      |    "Name": "Bug"
      |  },
      |  "Project": {
      |    "ResourceType": "Project",
      |    "Id": 12,
      |    "Name": "Metal RC"
      |  },
      |  "UserStory": {
      |    "ResourceType": "UserStory",
      |    "Id": 13,
      |    "Name": "Fail..."
      |  }
      |}""".stripMargin
  }

  def customFields(id: Long): String = {
    """{
      |  "ResourceType": "Assignable",
      |  "Id": 1,
      |  "Name": "Fail...",
      |  "Description": "",
      |  "StartDate": "/Date(1570812020000-0300)/",
      |  "EndDate": null,
      |  "CreateDate": "/Date(1570808853000-0300)/",
      |  "ModifyDate": "/Date(1571079788000-0300)/",
      |  "LastCommentDate": "/Date(1571070209000-0300)/",
      |  "Tags": "",
      |  "EntityType": {
      |    "ResourceType": "EntityType",
      |    "Id": 8,
      |    "Name": "Bug"
      |  },
      |  "CustomFields": [
      |    {
      |      "Name": "Request Type",
      |      "Type": "Text",
      |      "Value": "Issue"
      |    }
      |  ]
      |}""".stripMargin
  }
}

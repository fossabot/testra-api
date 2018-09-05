package tech.testra.reportal.exception

class ProjectAlreadyExistsException(name: String) : IllegalArgumentException("$name project already exists")
class TestScenarioAlreadyExistsException(name: String) : IllegalArgumentException("$name scenario already exists")
class TestCaseAlreadyExistsException(name: String) : IllegalArgumentException("$name test case already exists")

class ProjectNotFoundException(id: String) : IllegalArgumentException("Project not found for the id: $id")
class TestScenarioNotFoundException(id: String) : IllegalArgumentException("Scenario not found for the id: $id")
class TestCaseNotFoundException(id: String) : IllegalArgumentException("Test case not found for the id: $id")
class TestExecutionNotFoundException(id: String) : IllegalArgumentException("Test execution not found for the id: $id")
class TestResultNotFoundException(id: String) : IllegalArgumentException("Test status not found for the id: $id")

class InvalidGroupException(id: String) : IllegalArgumentException("Invalid group id in request : $id. Either feature id or namespace id.")

class ProjectTypeIsNotSimulationException(id: String) : IllegalArgumentException("Project $id is not type of Simulation")
class ProjectTypeIsNotSecurityException(id: String) : IllegalArgumentException("Project $id is not type of Security")

class QueryParamMissingException(param: String) : IllegalArgumentException("Required query param missing. param: $param")
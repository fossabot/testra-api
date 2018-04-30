package tech.testra.reportal.exception

class ProjectAlreadyExistsException(name: String) : IllegalArgumentException("$name project already exists")
class TestScenarioAlreadyExistsException(name: String) : IllegalArgumentException("$name scenario already exists")
class TestCaseAlreadyExistsException(name: String) : IllegalArgumentException("$name test case already exists")

class ProjectNotFoundException(id: String) : IllegalArgumentException("Project not found $id")
class TestScenarioNotFoundException(id: String) : IllegalArgumentException("Scenario not found $id")
class TestCaseNotFoundException(id: String) : IllegalArgumentException("Test case not found $id")
class TestExecutionNotFoundException(id: String) : IllegalArgumentException("Test execution not found $id")
class TestResultNotFoundException(id: String) : IllegalArgumentException("Test result not found $id")
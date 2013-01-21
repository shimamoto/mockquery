package jp.sf.amateras.mockquery

import org.specs2.mutable.Specification
import org.specs2.specification.After

import mock.JDBCMockObjectFactory

trait JdbcSpecification extends Specification {

  def verify(result: => MatchResult)(implicit m: JDBCTestModule) = try
    result.parameters.size match {
      // If not need to validate the parameters
      case 0      => m.verifySQLStatementExecuted(result.sql)
      case pCount => m.getExecutedSQLStatementParameterSets(result.sql).getParameterSet(0).size match {
        // If not match the count of placeholders and specified parameters
        case x if x != pCount =>
          failure("Expected %d parameter, actual %d parameter" format (pCount, x))
        case _ => result.parameters.zipWithIndex foreach { case (p, index) =>
          m.verifySQLStatementParameter(result.sql, 0, index + 1, p)
        }
      }
    }
    catch {
      case e: VerifyFailedException => failure(e.getMessage)
    }

  def debug(implicit m: JDBCTestModule) = {
    import scala.collection.JavaConversions._
    m.getExecutedSQLStatements foreach { s =>
      println("[debug] %s" format s)
    }
  }

  implicit def string2matchresult(sql: String): MatchResult = new MatchResult(sql)
  implicit def parametersForSql(sql: String) = new {
    def bind(params: Any*) = new MatchResult(sql, params:_*)
  }
}

trait JdbcContext { self: After =>
  val factory = new JDBCMockObjectFactory
  implicit val module = new JDBCTestModule(factory)
  module.setUseRegularExpressions(true)

  def after = {
    factory.restoreDrivers
  }
}
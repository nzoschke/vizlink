package vizlink

import org.slf4j.Logger
import org.slf4j.LoggerFactory

var Log = Logger()

class Logger {
  private val log: Logger = LoggerFactory.getLogger(Logger::class.java)

  fun v(tag: String, m: String) {
    log.info("${tag}: ${m}")
  }
}

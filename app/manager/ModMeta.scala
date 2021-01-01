package manager

import java.io.File

/**
 * @author jaron
 *         created on 2020/12/29 at 下午9:52
 */
case class ModMeta (id: String, name: String, description: String, dependencies: List[String], modDir: File)

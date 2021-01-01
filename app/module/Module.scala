package module

import com.google.inject.AbstractModule
import manager.ResourceManager

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ResourceManager])
      .to(classOf[ResourceManager])
      .asEagerSingleton()
  }
}

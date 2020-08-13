package module

import com.google.inject.AbstractModule
import manager.{BaseResourceManager, ResourceManager}

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ResourceManager])
      .to(classOf[BaseResourceManager])
      .asEagerSingleton()
  }
}
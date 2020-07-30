package module

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import manager.{BaseResourceManager, ResourceManager}

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ResourceManager])
      .annotatedWith(Names.named("memrm"))
      .to(classOf[BaseResourceManager])
      .asEagerSingleton()
  }
}
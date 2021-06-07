import logging as logger
logger.basicConfig(level="DEBUG")

from Service import ServiceState
from Service import ServiceKind
from VabIipOperationsBuilder import composeResult

# aligned to de.iip_ecosphere.platform.services.environment.ServiceMapper
NAME_SUBMODEL = "service"
NAME_PROP_ID = "id"
NAME_PROP_NAME = "name"
NAME_PROP_STATE = "state"
NAME_PROP_KIND = "kind"
NAME_PROP_VERSION = "version"
NAME_PROP_DESCRIPTION = "description"
NAME_PROP_DEPLOYABLE = "deployable"
NAME_PROP_TYPE = "type"
NAME_OP_ACTIVATE = "activate"
NAME_OP_PASSIVATE = "passivate"
NAME_OP_MIGRATE = "migrate"
NAME_OP_UPDATE = "update"
NAME_OP_SWITCH = "switchTo"
NAME_OP_RECONF = "reconfigure"
NAME_OP_SET_STATE = "setState"

def getQName(name, service):
    """Maps the given operation/property name for the given service to a qualified name.
    
    Parameters:
      name -- the name of the property/operation 
      service -- the service (context) the property/operation belongs to
    Returns:
      str
        the (qualified) name
    """ 
    
    return name
    #TODO return service.getId() + "_" + name

def mapService(builder, service):
    """Maps know functions of a service to functions and registers them appropriately with builder.
    
    Parameters:
      - builder the service builder (VabIipOperationsBuilder)
      - service the service to map (instance of Service)
    """

    def getId():
        return service.getId()
    builder.defineProperty(getQName(NAME_PROP_ID, service), getId, None)

    def getName():
        return service.getName()
    builder.defineProperty(getQName(NAME_PROP_NAME, service), getName, None)
    
    def getVersion():
        return service.getVersion().toString()
    builder.defineProperty(getQName(NAME_PROP_VERSION, service), getVersion, None)
    
    def getDescription():
        return service.getDescription()
    builder.defineProperty(getQName(NAME_PROP_DESCRIPTION, service), getDescription, None)
    
    def getState():
        return service.getState().name
    builder.defineProperty(getQName(NAME_PROP_STATE, service), getState, None)

    def isDeployable():
        return str(service.isDeployable())
    builder.defineProperty(getQName(NAME_PROP_DEPLOYABLE, service), isDeployable, None)
    
    def getKind():
        return service.getKind().name
    builder.defineProperty(getQName(NAME_PROP_KIND, service), getKind, None)

    def passivate(params):
        service.passivate() # ignore params
    builder.defineOperation(getQName(NAME_OP_PASSIVATE, service), passivate)

    def activate(params):
        service.activate() # ignore params
    builder.defineOperation(getQName(NAME_OP_ACTIVATE, service), activate)

    def setState(params):
        try:
            service.setState(ServiceState[params[0]])
            return composeResult(True, None)
        except ValueError as e:
            return composeResult(None, e.message)
    builder.defineOperation(getQName(NAME_OP_SET_STATE, service), setState)

    def migrate(params):
        service.migrate(params[0])
        return composeResult(True, None)
    builder.defineOperation(getQName(NAME_OP_MIGRATE, service), setState)

    def update(params):
        service.update(params[0])
        return composeResult(True, None)
    builder.defineOperation(getQName(NAME_OP_UPDATE, service), setState)

    def switch(params):
        service.switchTo(params[0])
        return composeResult(True, None)
    builder.defineOperation(getQName(NAME_OP_SWITCH, service), setState)

    def reconf(params):
        service.reconfigure(params[0], json.loads(params[1]))
        return composeResult(True, None)
    builder.defineOperation(getQName(NAME_OP_RECONF, service), setState)

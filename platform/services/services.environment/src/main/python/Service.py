from enum import Enum;
import logging as logger
logger.basicConfig(level="DEBUG")

class ServiceState(Enum):
    """Denotes the service state. Names and ordinals must be aligned with Java."""
    
    AVAILABLE = 0
    DEPLOYING = 1
    CREATED = 2
    STARTING = 3
    RUNNING = 4
    FAILED = 5
    STOPPING = 6
    STOPPED = 7
    PASSIVATING = 8
    PASSIVATED = 9
    MIGRATING = 10
    ACTIVATING = 11
    RECOVERING = 12
    RECOVERED = 13
    RECONFIGURING = 14
    UNDEPLOYING = 15
    UNKOWN = 16

class ServiceKind(Enum):
    """Denotes the service kind. Names and ordinals must be aligned with Java."""
    
    SOURCE_SERVICE = 0
    TRANSFORMATION_SERVICE = 1
    SINK_SERVICE = 2
    PROBE_SERVICE = 3

class Service:
    """Interface of an administrative service wrapper."""

    def getId():
        """Returns the unique id of the service.
        
        Returns:
          str
            The id of the service.
        """ 
        
        raise NotImplementedError
    
    def getName():
        """Returns the name of the service.
        
        Returns:
          str
            The name of the service.
        """ 

        raise NotImplementedError
    
    def getVersion():
        """Returns the version of the service.
        
        Returns:
          str
            The version of the service.
        """
        
        raise NotImplementedError
    
    def getDescription():
        """Returns the description of the service.
        
        Returns:
          str
            The description of the service.
        """
        raise NotImplementedError

    def getState():
        """Returns the state of the service. [R4c]
        
        Returns:
          ServiceState
            The state of the service.
        """
        
        raise NotImplementedError

    def setState(state:ServiceState):
        """Changes the state. [R133c]
        
        Parameters:
          - newState -- the new state (ServiceState)
        """
        
        raise NotImplementedError
    
    def isDeployable():
        """Returns whether the service is deployable in distributable manner or fixed in deployment location.
        
        Returns:
          bool
            Whether the state is deployable.
        """
        
        raise NotImplementedError
    
    def getKind():
        """Returns the service kind.
        
        Returns:
          ServiceKind
            The service kind.
        """
        
        raise NotImplementedError

    def migrateService(resourceId:str):
        """Migrates a service. However, it may be required to migrate/move the containing artifact. [adaptation]
        
        Parameters:
          resourceId -- the id of the resource to deploy to (str)
        """

        raise NotImplementedError
        
    def updateService(location):
        """Updates the service by the service in the given URL location. This operation is responsible for stopping
        the running service (if needed), replacing it, starting the new service.
        
        Parameters:
          location -- the location from where to update the service (str)
        """
        
        raise NotImplementedError
       
    def switchToService(targetId:str):
        """Switches to an interface-compatible service. This method cares for stopping the old service, performing
        a handover if adequate, starting the target service. [adaptation]
        
        Parameters:
          targetId -- the id of the target service (str)
        """
        
        raise NotImplementedError
   
    def activate():
        """Activates the service. [adaptation]"""
        
        raise NotImplementedError

    def passivate():
        """Passivates the service. [adaptation]"""
        
        raise NotImplementedError
    
    def reconfigure(values:dict):
        """Reconfigures the service. [adaptation]
        
        Parameters:
          - values -- the (service-specific) name-value mapping that shall lead to a reconfiguration of the service; 
            values come either as primitive values or as JSON structures complying with the parameter descriptor. The 
            service is responsible for correct JSON de-serialization according to its descriptor.
        """
        
        raise NotImplementedError

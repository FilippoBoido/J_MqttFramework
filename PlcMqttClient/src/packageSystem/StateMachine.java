package packageSystem;

public abstract class StateMachine {

	public enum E_StateMachine {
	    eInit,
	    eReady,
	    ePrepare,
	    eBusy,
	    eIdle,
	    eWaiting,
	    eError,
	    eShutDown
	  }
	
	
	protected boolean 	bFirstCall = true, 
						bInitOk = false,
						bReadyOk = false,
						bBusyOk = false,
						bErrorOk = false,
						bIdleOk = false,
						bWaitOk = false,
						bShutDownOk= false;
	
	protected int 	errorType,
					busyStep,
					readyStep,
					waitStep,
					errorStep,
					initStep,
					shutDownStep,
					idleStep;
	
	protected String exceptionMessage;
	
	public E_StateMachine eStateMachine;
	
	public StateMachine() {
		boot();	
	}
	
	private void resetOkStates()
	{
		bInitOk = false;
		bReadyOk = false;
		bBusyOk = false;
		bErrorOk = false;
		bIdleOk = false;
		bWaitOk = false;
		bShutDownOk = false;
	}
	
	private void resetSteps()
	{
		busyStep = 0;
		readyStep = 0;
		waitStep = 0;
		errorStep = 0;
		initStep = 0;
		idleStep = 0;
	}
	
	public static String getStateAsString(int state)
	{
		switch(state)
		{
		
		case 0:
			return "No state";
			
		case 1:
			return "Init";
			
		case 2:
			return "Ready";
			
		case 3:
			return "Prepare";
			
		case 4:
			return "Busy";
			
		case 5: 
			return "Waiting";
			
		case 6: 
			return "Idle";
			
		case 7: 
			return "Error";	
			
		case 8: 
			return "Shutting down";	
			
		}
			
		return "No state";		
	}
	
	public void checkStateMachine() throws Throwable
	{
		switch(eStateMachine)
		{
			case eInit: 
				init();
				bInitOk = true;
				eStateMachine = E_StateMachine.eWaiting;
				break;
				
			case eReady:
				ready();
				break;
				
			case ePrepare: 
				prepare();
				eStateMachine = E_StateMachine.eBusy;
				break;
				
			case eBusy:
				busy();
				break;
				
			case eIdle:
				idle();
				eStateMachine = E_StateMachine.eReady;
				break;
				
			case eWaiting:
				waiting();
				break;
				
			case eError:
				error();
				break;	
				
			case eShutDown:
				shuttingDown();
				break;	
		}
	}
	
	//States
	protected abstract void init() throws Throwable;
	protected abstract void ready() throws Throwable;
	protected abstract void prepare() throws Throwable;
	protected abstract void busy() throws Throwable;
	protected abstract void idle() throws Throwable;
	protected abstract void waiting() throws Throwable;
	protected abstract void error() throws Throwable;
	protected abstract void shuttingDown() throws Throwable;
	
	public boolean isInit()
	{
		return (E_StateMachine.eInit == eStateMachine);
	}
	
	public boolean isReady()
	{
		return (E_StateMachine.eReady == eStateMachine);
	}
	
	public boolean isBusy()
	{
		return (E_StateMachine.eBusy == eStateMachine);
	}
	
	public boolean isError()
	{
		return (E_StateMachine.eError == eStateMachine);
	}
	
	public boolean isPreparing()
	{
		return (E_StateMachine.ePrepare == eStateMachine);
	}
	
	public boolean isIdle()
	{
		return (E_StateMachine.eIdle == eStateMachine);
	}
	
	public boolean isShuttingDown()
	{
		return (E_StateMachine.eShutDown == eStateMachine);
	}
	
	public boolean isInitOk()
	{
		return bInitOk;
	}
	
	public boolean isReadyOk()
	{
		return bReadyOk;
	}
	
	public boolean isBusyOk()
	{
		return bBusyOk;
	}
	
	public boolean isErrorOk()
	{
		return bErrorOk;
	}
	
	public boolean isWaitOk()
	{
		return bWaitOk;
	}
	
	public boolean isIdleOk()
	{
		return bIdleOk;
	}
	
	public boolean isShutDownOk()
	{
		return bShutDownOk;
	}
	
	
	public boolean isWaiting()
	{
		return (E_StateMachine.eWaiting == eStateMachine);
	}
	
	//Cmds
	protected boolean boot()
	{
		if(bFirstCall)
		{
			bFirstCall = false;
			changeState(E_StateMachine.eInit);
			return true;
		}
		
		return false;
	}
	
	public boolean done()
	{
		if(eStateMachine == E_StateMachine.eBusy)
		{
			changeState(E_StateMachine.eIdle);
			return true;
		}
		
		return false;
	}
	
	public boolean endWait()
	{
		if(eStateMachine == E_StateMachine.eWaiting)
		{
			changeState(E_StateMachine.eBusy);
			return true;
		}
		
		return false;
	}
	
	public boolean execute()
	{
		if(		eStateMachine == E_StateMachine.eReady
			|| 	eStateMachine == E_StateMachine.eWaiting)
		{
			changeState(E_StateMachine.ePrepare);
			return true;
		}
		
		return false;
	}
	
	public boolean fault(int errorType)
	{
		if(		eStateMachine == E_StateMachine.eBusy
			|| 	eStateMachine == E_StateMachine.eInit
			|| 	eStateMachine == E_StateMachine.eReady
			|| 	eStateMachine == E_StateMachine.eWaiting)
		{
			this.errorType = errorType;
			changeState(E_StateMachine.eError);
			return true;
		}
		
		return false;
	}
	
	public boolean fault()
	{
		if(fault(0))
			return true;
		
		return false;
	}
	
	public boolean reset()
	{
		if(eStateMachine == E_StateMachine.eError)
		{
			changeState(E_StateMachine.eReady);
			return true;
		}
		
		return false;
	}
	
	public boolean start()
	{
		if(		eStateMachine == E_StateMachine.eInit
			|| 	eStateMachine == E_StateMachine.eWaiting)
		{
			changeState(E_StateMachine.eReady);
			return true;
		}
		
		return false;
	}
	
	public void shutDown()
	{
		changeState(E_StateMachine.eShutDown);
	}
	public boolean waitLoop()
	{
		if(eStateMachine == E_StateMachine.eBusy)
		{
			changeState(E_StateMachine.eWaiting);
			return true;
		}
		
		return false;
	}
	
	private void changeState(E_StateMachine eStateMachine)
	{
		resetOkStates();
		resetSteps();
		this.eStateMachine = eStateMachine;
	}
	
	
	
}

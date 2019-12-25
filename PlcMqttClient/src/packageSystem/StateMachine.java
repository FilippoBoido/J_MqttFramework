package packageSystem;

public abstract class StateMachine {

	public enum E_StateMachine {
	    eInit,
	    eReady,
	    ePrepare,
	    eBusy,
	    eIdle,
	    eWaiting,
	    eError
	  }
	
	
	private boolean bFirstCall = true;
	protected int errorType;
	public E_StateMachine eStateMachine;
	
	public StateMachine() {
		Boot();	
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
			
		
			
		}
		
			
		return "No state";
		
	}
	
	public void CheckStateMachine()
	{
		switch(eStateMachine)
		{
			case eInit: 
				Init();
				break;
				
			case eReady:
				Ready();
				break;
				
			case ePrepare: 
				Prepare();
				eStateMachine = E_StateMachine.eBusy;
				break;
				
			case eBusy:
				Busy();
				break;
				
			case eIdle:
				Idle();
				eStateMachine = E_StateMachine.eReady;
				break;
				
			case eWaiting:
				Waiting();
				break;
				
			case eError:
				Error();
				break;
		
		}
	}
	
	//States
	protected abstract void Init();
	protected abstract void Ready();
	protected abstract void Prepare();
	protected abstract void Busy();
	protected abstract void Idle();
	protected abstract void Waiting();
	protected abstract void Error();
	
	
	//Cmds
	protected boolean Boot()
	{
		if(bFirstCall)
		{
			bFirstCall = false;
			ChangeState(E_StateMachine.eInit);
			return true;
		}
		
		return false;
	}
	
	protected boolean Done()
	{
		if(eStateMachine == E_StateMachine.eBusy)
		{
			ChangeState(E_StateMachine.eIdle);
			return true;
		}
		
		return false;
	}
	
	public boolean EndWait()
	{
		if(eStateMachine == E_StateMachine.eWaiting)
		{
			ChangeState(E_StateMachine.eBusy);
			return true;
		}
		
		return false;
	}
	
	public boolean Execute()
	{
		if(eStateMachine == E_StateMachine.eReady)
		{
			ChangeState(E_StateMachine.ePrepare);
			return true;
		}
		
		return false;
	}
	
	protected boolean Fault(int errorType)
	{
		if(eStateMachine == E_StateMachine.eBusy
				|| eStateMachine == E_StateMachine.eInit
				|| eStateMachine == E_StateMachine.eReady)
		{
			this.errorType = errorType;
			ChangeState(E_StateMachine.eError);
			return true;
		}
		
		return false;
	}
	
	public boolean Reset()
	{
		if(eStateMachine == E_StateMachine.eError)
		{
			ChangeState(E_StateMachine.eReady);
			return true;
		}
		
		return false;
	}
	
	protected boolean Start()
	{
		if(eStateMachine == E_StateMachine.eInit)
		{
			ChangeState(E_StateMachine.eReady);
			return true;
		}
		
		return false;
	}
	
	protected boolean Wait()
	{
		if(eStateMachine == E_StateMachine.eBusy)
		{
			ChangeState(E_StateMachine.eWaiting);
			return true;
		}
		
		return false;
	}
	
	private void ChangeState(E_StateMachine eStateMachine)
	{
		this.eStateMachine = eStateMachine;
	}
	
	
	
}

package packageSystem;

public abstract class StateMachine {

	enum E_StateMachine {
	    eInit,
	    eReady,
	    ePrepare,
	    eBusy,
	    eIdle,
	    eWaiting,
	    eError
	  }
	
	
	private boolean bFirstCall = true;
	public E_StateMachine eStateMachine;
	
	public StateMachine() {
		Boot();	
	}
	
	
	public void checkStateMachine()
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
	
	protected boolean Fault()
	{
		if(eStateMachine == E_StateMachine.eBusy)
		{
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

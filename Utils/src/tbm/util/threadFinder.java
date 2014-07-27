package tbm.util;
//http://nadeausoftware.com/articles/2008/04/java_tip_how_list_and_find_threads_and_thread_groups#Gettingathreadbyname
public class threadFinder {
	private static ThreadGroup rootThreadGroup = null;
	 
	public static ThreadGroup getRootThreadGroup( ) {
	    if ( rootThreadGroup != null )
	        return rootThreadGroup;
	    ThreadGroup tg = Thread.currentThread( ).getThreadGroup( );
	    ThreadGroup ptg;
	    while ( (ptg = tg.getParent( )) != null )
	        tg = ptg;
	    return tg;
	}

	ThreadGroup[] getAllThreadGroups( ) {
	    final ThreadGroup root = getRootThreadGroup( );
	    int nAlloc = root.activeGroupCount( );
	    int n = 0;
	    ThreadGroup[] groups;
	    do {
	        nAlloc *= 2;
	        groups = new ThreadGroup[ nAlloc ];
	        n = root.enumerate( groups, true );
	    } while ( n == nAlloc );
	 
	    ThreadGroup[] allGroups = new ThreadGroup[n+1];
	    allGroups[0] = root;
	    System.arraycopy( groups, 0, allGroups, 1, n );
	    return allGroups;
	}

	ThreadGroup getThreadGroup( final String name ) {
	    if ( name == null )
	        throw new NullPointerException( "Null name" );
	    final ThreadGroup[] groups = getAllThreadGroups( );
	    for ( ThreadGroup group : groups )
	        if ( group.getName( ).equals( name ) )
	            return group;
	    return null;
	}
}

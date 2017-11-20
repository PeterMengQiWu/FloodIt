import java.io.*;
public class GenericLinkedStack<E> implements Stack<E>, Serializable{
	private static class Elem<T> implements Serializable{
		private T value;
		private Elem<T> next;

		private Elem(T value, Elem<T> next){
			this.value = value;
			this.next = next;
		}
	}

	private Elem<E> top;
	private int size;

	public GenericLinkedStack(){
		top = null;
		size = 0;
	}

	public int getSize(){
		return size;
	}

	public boolean isEmpty(){
		return size == 0;
	}

	public void push(E newVal){
		if(newVal == null){
			throw new NullPointerException("Cannot push null references");
		}
		top = new Elem<E>(newVal, top);
		size++;
		
	}

	public E peek(){
		if(isEmpty()){
			throw new EmptyStackException();
		}
		return top.value;
	}

	public E pop(){
		if(isEmpty()){
			throw new EmptyStackException();
		}
		
		E value = top.value;
		top = top.next;
		size--;
		return value;

	}

	public void clear(){
		top = null;
		size = 0;
	}
}
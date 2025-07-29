package de.larphelden.larp_app.exceptions;

public class StorageException extends RuntimeException{

  public StorageException(String message){
    super(message);

  }

  public StorageException(String message, Throwable cause){
    super(message, cause);
  }
}

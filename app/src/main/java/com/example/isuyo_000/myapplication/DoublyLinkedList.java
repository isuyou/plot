package com.example.isuyo_000.myapplication;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class DoublyLinkedList<Item> implements Iterable<Item> {

    //front cursor of the list
    private Node front;
    //end cursor of the list
    private Node back;
    //size of the list
    private int size;





    private class Node{
        private Item item;
        private Node previous;
        private Node next;

        private Node(Item item, Node previous, Node next){
            this.item = item;
            this.previous = previous;
            this.next = next;
        }
    }

    //returns DoublyLinkedListIterator
    public ListIterator<Item> iterator()  { return new DoublyLinkedListIterator(); }

    //Iterator implementation of Double Linked List
    private class DoublyLinkedListIterator implements ListIterator<Item> {
        private Node current      = front;  // the node that is returned by next()
        private Node lastAccessed = null;      // the last node to be returned by prev() or next()
        // reset to null upon intervening remove() or add()
        private int index = 0;

        @Override
        public boolean hasNext() {
            return (current != null);
        }

        @Override
        public Item next() {
            Item item = current.item;
            current = current.next;
            return item;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public Item previous() {
            return null;
        }

        @Override
        public int nextIndex() {
            return 0;
        }

        @Override
        public int previousIndex() {
            return 0;
        }

        @Override
        public void remove() {

        }

        @Override
        public void set(Item item) {

        }

        @Override
        public void add(Item item) {

        }
    }
}
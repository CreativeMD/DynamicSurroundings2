/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.lib.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;
import org.orecruncher.lib.math.MathStuff;

import net.minecraft.util.math.MathHelper;

public class ObjectArray<T> implements Collection<T> {
    
    private static final int DEFAULT_SIZE = 4;
    
    protected Object[] data;
    protected int insertionIdx;
    
    public ObjectArray() {
        this(DEFAULT_SIZE);
    }
    
    public ObjectArray(final int size) {
        this.data = new Object[size];
    }
    
    public ObjectArray(@Nonnull final ObjectArray<T> input) {
        this.data = Arrays.copyOf(input.data, input.size());
        this.insertionIdx = input.insertionIdx;
    }
    
    public ObjectArray(@Nonnull final T[] input) {
        this.data = Arrays.copyOf(input, input.length);
        this.insertionIdx = input.length;
    }
    
    private void resize() {
        final int newSize = MathHelper.smallestEncompassingPowerOfTwo(MathStuff.max(this.data.length * 2, DEFAULT_SIZE));
        final Object[] t = new Object[newSize];
        if (this.data.length > 0)
            System.arraycopy(this.data, 0, t, 0, this.data.length);
        this.data = t;
    }
    
    @Override
    public int size() {
        return this.insertionIdx;
    }
    
    @SuppressWarnings("unchecked")
    public T get(final int idx) {
        if (idx >= 0 && idx < this.insertionIdx)
            return (T) this.data[idx];
        return null;
    }
    
    private void remove0(final int idx) {
        final Object m = this.data[--this.insertionIdx];
        this.data[this.insertionIdx] = null;
        if (idx < this.insertionIdx)
            this.data[idx] = m;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean removeIf(@Nonnull final Predicate<? super T> pred) {
        boolean result = false;
        for (int i = this.insertionIdx - 1; i >= 0; i--) {
            final T t = (T) this.data[i];
            if (pred.test(t)) {
                result = true;
                this.remove0(i);
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void forEach(@Nonnull final Consumer<? super T> consumer) {
        for (int i = 0; i < this.insertionIdx; i++)
            consumer.accept((T) this.data[i]);
    }
    
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }
    
    private int find(@Nonnull final Object o) {
        for (int i = 0; i < this.insertionIdx; i++)
            if (o.equals(this.data[i]))
                return i;
        return -1;
    }
    
    @Override
    public boolean contains(@Nonnull final Object o) {
        return find(o) != -1;
    }
    
    @Override
    @Nonnull
    public Object[] toArray() {
        Object[] result = ArrayUtils.EMPTY_OBJECT_ARRAY;
        if (this.insertionIdx > 0) {
            result = new Object[this.insertionIdx];
            System.arraycopy(this.data, 0, result, 0, this.insertionIdx);
        }
        return result;
    }
    
    @SuppressWarnings({ "unchecked", "hiding" })
    @Override
    @Nonnull
    public <T> T[] toArray(@Nonnull final T[] a) {
        // From ArrayList impl
        if (a.length < this.insertionIdx)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(this.data, this.insertionIdx, a.getClass());
        System.arraycopy(this.data, 0, a, 0, this.insertionIdx);
        if (a.length > this.insertionIdx)
            a[this.insertionIdx] = null;
        return a;
    }
    
    @Override
    public boolean add(@Nonnull final T e) {
        if (this.data.length == this.insertionIdx)
            resize();
        this.data[this.insertionIdx++] = e;
        return true;
    }
    
    @Override
    public boolean remove(@Nonnull final Object o) {
        final int idx = find(o);
        if (idx != -1)
            this.remove0(idx);
        return idx != -1;
    }
    
    @Override
    public boolean containsAll(@Nonnull final Collection<?> c) {
        for (final Object obj : c)
            if (!this.contains(obj))
                return false;
        return true;
    }
    
    @Override
    public boolean addAll(@Nonnull final Collection<? extends T> c) {
        boolean result = false;
        for (final T element : c)
            result |= this.add(element);
        return result;
    }
    
    public boolean addAll(@Nonnull final T[] list) {
        boolean result = false;
        for (final T t : list)
            result |= this.add(t);
        return result;
    }
    
    @Override
    public boolean removeAll(@Nonnull final Collection<?> c) {
        return this.removeIf(c::contains);
    }
    
    @Override
    public boolean retainAll(@Nonnull final Collection<?> c) {
        return this.removeIf(entry -> !c.contains(entry));
    }
    
    public void trim() {
        if (this.insertionIdx < this.data.length) {
            if (this.insertionIdx == 0) {
                this.data = ArrayUtils.EMPTY_OBJECT_ARRAY;
            } else {
                final Object[] t = new Object[this.insertionIdx];
                System.arraycopy(this.data, 0, t, 0, this.insertionIdx);
                this.data = t;
            }
        }
    }
    
    @Override
    public void clear() {
        for (int i = 0; i < this.insertionIdx; i++)
            this.data[i] = null;
        this.insertionIdx = 0;
    }
    
    @Override
    @Nonnull
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            
            private int idx = -1;
            
            @Override
            public boolean hasNext() {
                return (this.idx + 1) < ObjectArray.this.insertionIdx;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                return (T) ObjectArray.this.data[++this.idx];
            }
            
        };
    }
    
}

import { writable } from 'svelte/store';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'info';
  message: string;
  timeout?: number;
}

function createNotifications() {
  const { subscribe, update, set } = writable<Notification[]>([]);

  function push(n: Omit<Notification, 'id'>) {
    const id = crypto.randomUUID();
    const note: Notification = { id, timeout: 4000, ...n };
    update(list => [...list, note]);
    if (note.timeout) {
      setTimeout(() => dismiss(id), note.timeout);
    }
    return id;
  }

  function dismiss(id: string) {
    update(list => list.filter(n => n.id !== id));
  }

  function clear() { set([]); }

  return { subscribe, push, dismiss, clear };
}

export const notifications = createNotifications();

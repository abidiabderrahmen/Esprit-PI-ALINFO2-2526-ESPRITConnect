import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './events.component.html',
  styleUrl: './events.component.scss'
})
export class EventsComponent implements OnInit {
  isLoading = true;
  selectedType = 'all';
  showCreateModal = false;
  isCreating = false;
  createError = '';

  newEvent = {
    title: '',
    description: '',
    event_type: 'CONFERENCE',
    location: '',
    date: '',
    time: '',
    max_participants: 50,
    is_online: false,
    meeting_link: ''
  };

  types = [
    { value: 'all', label: 'All Events', icon: 'event' },
    { value: 'conference', label: 'Conferences', icon: 'groups' },
    { value: 'workshop', label: 'Workshops', icon: 'build' },
    { value: 'career_fair', label: 'Career Fairs', icon: 'work' },
    { value: 'webinar', label: 'Webinars', icon: 'videocam' },
  ];

  eventTypes = [
    { value: 'CONFERENCE', label: 'Conference' },
    { value: 'WORKSHOP', label: 'Workshop' },
    { value: 'CAREER_FAIR', label: 'Career Fair' },
    { value: 'WEBINAR', label: 'Webinar' },
    { value: 'MEETUP', label: 'Meetup' },
  ];

  featuredEvent: any = null;
  events: any[] = [];
  private eventColors = ['#4338CA', '#E63946', '#059669', '#EA580C', '#7C3AED', '#DB2777', '#0891B2', '#65A30D'];

  constructor(private apiService: ApiService) {}

  ngOnInit() { this.loadEvents(); }

  loadEvents() {
    this.isLoading = true;
    this.apiService.getEvents().subscribe({
      next: (res) => {
        const results = res.results || res;
        const mapped = (Array.isArray(results) ? results : []).map((e: any, i: number) => {
          const d = new Date(e.date);
          const months = ['JAN','FEB','MAR','APR','MAY','JUN','JUL','AUG','SEP','OCT','NOV','DEC'];
          return {
            id: e.id,
            title: e.title,
            type: (e.event_type || 'conference').toLowerCase(),
            month: months[d.getMonth()] || 'JAN',
            day: String(d.getDate()).padStart(2, '0'),
            bg: this.eventColors[i % this.eventColors.length],
            description: e.description,
            date: e.date,
            time: e.time,
            location: e.location,
            capacity: e.max_participants,
            registered: e.registered_count || 0,
            online: e.is_online,
            isRegistered: e.is_registered || false
          };
        });

        if (mapped.length > 0) {
          const feat = mapped[0];
          this.featuredEvent = {
            id: feat.id,
            title: feat.title, type: feat.type, description: feat.description,
            date: feat.date, month: feat.month, day: feat.day,
            year: new Date(feat.date).getFullYear().toString(),
            time: feat.time, location: feat.location,
            attendees: feat.registered, isRegistered: feat.isRegistered
          };
          this.events = mapped.slice(1);
        } else {
          this.featuredEvent = null;
          this.events = [];
        }
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  registerForFeatured() {
    if (!this.featuredEvent?.id) return;
    this.apiService.registerForEvent(this.featuredEvent.id).subscribe({
      next: () => {
        this.featuredEvent.attendees++;
        this.featuredEvent.isRegistered = true;
      },
      error: (err) => {
        const msg = err.error?.detail || 'Registration failed';
        alert(msg);
      }
    });
  }

  unregisterFromFeatured() {
    if (!this.featuredEvent?.id) return;
    this.apiService.unregisterFromEvent(this.featuredEvent.id).subscribe({
      next: () => {
        this.featuredEvent.attendees = Math.max(0, this.featuredEvent.attendees - 1);
        this.featuredEvent.isRegistered = false;
      }
    });
  }

  registerForEvent(eventId: number) {
    this.apiService.registerForEvent(eventId).subscribe({
      next: () => {
        const ev = this.events.find(e => e.id === eventId);
        if (ev) { ev.registered++; ev.isRegistered = true; }
      },
      error: (err) => {
        const msg = err.error?.detail || 'Registration failed';
        alert(msg);
      }
    });
  }

  unregisterFromEvent(eventId: number) {
    this.apiService.unregisterFromEvent(eventId).subscribe({
      next: () => {
        const ev = this.events.find(e => e.id === eventId);
        if (ev) { ev.registered = Math.max(0, ev.registered - 1); ev.isRegistered = false; }
      }
    });
  }

  toggleEventRegistration(event: any) {
    if (event.isRegistered) {
      this.unregisterFromEvent(event.id);
    } else {
      this.registerForEvent(event.id);
    }
  }

  openCreateModal() {
    this.showCreateModal = true;
    this.createError = '';
    this.newEvent = {
      title: '', description: '', event_type: 'CONFERENCE', location: '',
      date: '', time: '', max_participants: 50, is_online: false, meeting_link: ''
    };
  }

  closeCreateModal() {
    this.showCreateModal = false;
  }

  submitEvent() {
    if (!this.newEvent.title || !this.newEvent.date || !this.newEvent.time || !this.newEvent.location) {
      this.createError = 'Please fill in all required fields.';
      return;
    }
    this.isCreating = true;
    this.createError = '';

    this.apiService.createEvent(this.newEvent).subscribe({
      next: () => {
        this.isCreating = false;
        this.showCreateModal = false;
        this.loadEvents();
      },
      error: (err) => {
        this.isCreating = false;
        this.createError = err.error?.detail || err.error?.title?.[0] || 'Failed to create event. Please try again.';
      }
    });
  }

  get filteredEvents() {
    if (this.selectedType === 'all') return this.events;
    return this.events.filter(e => e.type === this.selectedType);
  }
}

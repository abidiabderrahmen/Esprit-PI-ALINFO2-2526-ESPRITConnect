import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss'
})
export class LandingComponent {
  isScrolled = false;

  @HostListener('window:scroll')
  onScroll() { this.isScrolled = window.scrollY > 20; }

  partners = ['Google', 'Vermeg', 'Sofrecom', 'InstaDeep', 'Talan', 'Microsoft', 'AWS', 'Orange'];

  features = [
    { icon: 'people', title: 'Alumni Directory', desc: 'Search and connect with graduates by industry, skills, location, and graduation year.', bg: 'linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%)', color: '#4338CA' },
    { icon: 'work_outline', title: 'Job Board', desc: 'Access exclusive jobs, internships, and freelance opportunities from top companies.', bg: 'linear-gradient(135deg, #FFF1F2 0%, #FFE4E6 100%)', color: '#E63946' },
    { icon: 'event', title: 'Events Hub', desc: 'Discover and join conferences, workshops, career fairs, and networking events.', bg: 'linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%)', color: '#059669' },
    { icon: 'forum', title: 'Community Feed', desc: 'Share achievements, publish articles, ask questions, and engage with peers.', bg: 'linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%)', color: '#EA580C' },
    { icon: 'school', title: 'Mentorship', desc: 'Get paired with experienced alumni mentors who guide your career journey.', bg: 'linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%)', color: '#7C3AED' },
    { icon: 'auto_awesome', title: 'Smart Matching', desc: 'AI-powered recommendations for jobs, events, and professional connections.', bg: 'linear-gradient(135deg, #FEF9C3 0%, #FEF08A 100%)', color: '#CA8A04' },
  ];

  stats = [
    { icon: 'people', value: '2,500+', label: 'Active Members', color: '#818CF8' },
    { icon: 'business', value: '150+', label: 'Partner Companies', color: '#FB7185' },
    { icon: 'work', value: '500+', label: 'Jobs Posted', color: '#34D399' },
    { icon: 'emoji_events', value: '95%', label: 'Employment Rate', color: '#FBBF24' },
  ];

  steps = [
    { icon: 'person_add', title: 'Create Your Profile', desc: 'Sign up and showcase your skills, experience, and career goals to the community.' },
    { icon: 'connect_without_contact', title: 'Connect & Engage', desc: 'Discover alumni, join groups, attend events, and find mentors in your field.' },
    { icon: 'rocket_launch', title: 'Launch Your Career', desc: 'Apply to exclusive jobs, get referrals, and accelerate your professional growth.' },
  ];

  testimonials = [
    { text: 'ESPRIT CONNECT helped me land my dream job at Google. The alumni network is incredibly supportive and the platform made networking effortless.', name: 'Sami Trabelsi', role: 'Software Engineer, Google', initials: 'ST', color: '#4338CA' },
    { text: 'As a recruiter, this platform gives me direct access to the best ESPRIT talent. The quality of candidates is exceptional and the process is seamless.', name: 'Nadia Ben Ali', role: 'HR Manager, Vermeg', initials: 'NA', color: '#E63946' },
    { text: 'The mentorship program completely transformed my career trajectory. My mentor guided me from junior dev to team lead in just 2 years.', name: 'Youssef Gharbi', role: 'Data Scientist, Meta', initials: 'YG', color: '#059669' },
  ];
}

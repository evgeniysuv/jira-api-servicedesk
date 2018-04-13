package model;

import java.time.LocalDateTime;

public class Ticket {
    private String ticketKey;
    private String type;
    private String status;
    private LocalDateTime created;
    private String description;
    private String priority;
    private String paid;
    private String elapsedTime;
    private Long remainingTime;
    private double minutesOfSupport;

    public Ticket(String ticketKey) {
        this.ticketKey = ticketKey;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketKey='" + ticketKey + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", created='" + created + '\'' +
                ", description='" + description + '\'' +
                ", priority='" + priority + '\'' +
                ", paid='" + paid + '\'' +
                ", elapsedTime='" + elapsedTime + '\'' +
                ", remainingTime='" + remainingTime + '\'' +
                '}';
    }

    public String getTicketKey() {
        return ticketKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(Long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public double getMinutesOfSupport() {
        return minutesOfSupport;
    }

    public void setMinutesOfSupport(double minutesOfSupport) {
        this.minutesOfSupport = minutesOfSupport;
    }
}

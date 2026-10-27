package com.example.ThucTapThucTe.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_groups")
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @ManyToOne
    @JoinColumn(name = "training_class_id", nullable = false)
    private training_class trainingClass;

    @ManyToMany
    @JoinTable(name = "chat_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    protected ChatGroup() {
    }

    public ChatGroup(String name, User mentor, training_class trainingClass) {
        this.name = name;
        this.mentor = mentor;
        this.trainingClass = trainingClass;
        this.members.add(mentor);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public User getMentor() { return mentor; }
    public training_class getTrainingClass() { return trainingClass; }
    public Set<User> getMembers() { return members; }
    public void addMember(User user) { members.add(user); }
    public boolean hasMember(Long userId) { return members.stream().anyMatch(user -> user.getId().equals(userId)); }
}
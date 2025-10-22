package com.simplecommerce.actor.bot;

import com.simplecommerce.actor.ActorEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Bot entity representing automated systems and external applications.
 * Bots are used for automation, integrations, and external app access.
 *
 * @author julius.krah
 */
@Entity(name = "Bot")
@Table(name = "bots")
public class BotEntity extends ActorEntity {

  @Column(unique = true)
  private String apiKey;

  @Column
  private String appId;

  @Column(columnDefinition = "TEXT")
  private String permissions; // JSON array of permissions

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getPermissions() {
    return permissions;
  }

  public void setPermissions(String permissions) {
    this.permissions = permissions;
  }

  @Override
  public String toString() {
    return "BotEntity{" +
        "id=" + getId() +
        ", username='" + getUsername() + '\'' +
        ", email='" + getEmail() + '\'' +
        ", appId='" + appId + '\'' +
        ", externalId='" + getExternalId() + '\'' +
        ", createdAt=" + getCreatedDate() +
        ", updatedAt=" + getLastModifiedDate() +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BotEntity botEntity)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return Objects.equals(apiKey, botEntity.apiKey) && Objects.equals(appId, botEntity.appId) && Objects.equals(permissions, botEntity.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), apiKey, appId, permissions);
  }
}
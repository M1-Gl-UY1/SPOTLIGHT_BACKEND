const { DataTypes } = require("sequelize");
const bcrypt = require("bcrypt");

const EMAIL_REGEX = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
const NAME_REGEX = /^[a-zA-Z0-9\s]+$/; // Ajout \s pour les noms composés
const PHONE_REGEX = /^\d{9,13}$/;

module.exports = (sequelize) => {
  const User = sequelize.define(
    "User",
    {
      firstName: {
        type: DataTypes.STRING,
        allowNull: false,
        validate: {
          notEmpty: { msg: "firstName is required" },
          is: { args: NAME_REGEX, msg: "Invalid firstName format" },
        },
      },
      lastName: {
        type: DataTypes.STRING,
        allowNull: false,
        validate: {
          notEmpty: { msg: "lastName is required" },
          is: { args: NAME_REGEX, msg: "Invalid lastName format" },
        },
      },
      email: {
        type: DataTypes.STRING,
        unique: true,
        allowNull: false,
        validate: {
          isEmail: true,
          is: { args: EMAIL_REGEX, msg: "Invalid email format" },
        },
      },
      phone: {
        type: DataTypes.STRING,
        allowNull: false,
        unique: true,
        validate: {
          is: { args: PHONE_REGEX, msg: "Phone must contain 9–13 digits" },
        },
      },
      country: {
        type: DataTypes.STRING,
        allowNull: true,
      },
      password: {
        type: DataTypes.STRING,
        allowNull: false,
      },
      avatar: {
        type: DataTypes.STRING,
        defaultValue: "",
      },
      role: {
        type: DataTypes.STRING,
        defaultValue: "user", // 'user', 'provider', 'admin'
      },
      // --- AJOUT POUR LA MODÉRATION ---
      status: {
        type: DataTypes.ENUM('ACTIF', 'SUSPENDU', 'BANNI'),
        defaultValue: 'ACTIF',
        allowNull: false
      },
      refreshToken: {
        type: DataTypes.JSON, // Préférez JSON pour les tableaux en MySQL/PG modernes, sinon STRING pour compatibilité
        defaultValue: [],
      },
    },
    {
      tableName: "users",
      freezeTableName: true,
      timestamps: true,
      hooks: {
        async beforeSave(user) {
          if (user.changed("password")) {
            const salt = await bcrypt.genSalt(10);
            user.password = await bcrypt.hash(user.password, salt);
          }
        },
      },
    }
  );

  // Instance methods
  User.prototype.matchPassword = async function (enteredPassword) {
    return bcrypt.compare(enteredPassword, this.password);
  };

  User.associate = (models) => {
    // Assure-toi que les modèles ReportUser et Provider existent dans ton dossier models
    if(models.ReportUser) {
        User.hasMany(models.ReportUser, { foreignKey: 'reporterId', as: 'reportsMade' });
        User.hasMany(models.ReportUser, { foreignKey: 'reportedUserId', as: 'reportsReceived' });
    }
    if(models.Provider) {
        User.hasOne(models.Provider, { foreignKey: 'userId', as: 'providerProfile' });
    }
  };

  return User;
};
const { DataTypes } = require("sequelize");

module.exports = (sequelize) => {
  const Provider = sequelize.define(
    "Provider",
    {
      name: {
        type: DataTypes.STRING,
        allowNull: false,
        validate: {
          notEmpty: { msg: "Provider name is required" },
        },
      },

      certifications: {
        type: DataTypes.STRING,
        allowNull: true,
      },

      nationalId: {
        type: DataTypes.STRING,
        allowNull: true,
      },

      phone: {
        type: DataTypes.STRING,
        unique: {
          msg: "Provider phone number already exists",
        },
        allowNull: true,
        validate: {
          is: {
            args: /^\d{9,13}$/,
            msg: "Phone must contain between 9–13 digits",
          },
        },
      },

      email: {
        type: DataTypes.STRING,
        unique: {
          msg: "Provider email already exists",
        },
        allowNull: true,
        validate: {
          isEmail: {
            msg: "Invalid provider email format",
          },
        },
      },

      rating: {
        type: DataTypes.FLOAT,
        defaultValue: 0,
      },

      degrees: {
        type: DataTypes.STRING,
        allowNull: true,
      },

      userId: {
        type: DataTypes.INTEGER,
        allowNull: false,
        /*unique: {
          msg: "A user may only have one provider profile",
        },*/
      },
    },
    {
      tableName: "providers",
      timestamps: true,
    }
  );

    Provider.associate = (models) => {
      Provider.belongsTo(models.User, { foreignKey: 'userId', as: 'user' });
    };

  return Provider;
};

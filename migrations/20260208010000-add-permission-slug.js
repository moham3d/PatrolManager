'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    // 1. Add slug column (nullable first)
    await queryInterface.addColumn('Permissions', 'slug', {
      type: Sequelize.STRING,
      allowNull: true,
      after: 'name'
    });
    console.log('✅ Added slug column');

    // 2. Update existing permissions with slugs based on their names
    // Map name to slug (e.g., "View Sites" -> "VIEW_SITES")
    const { sequelize } = queryInterface;
    const [results] = await sequelize.query('SELECT id, name FROM Permissions');
    console.log(`Found ${results.length} existing permissions`);

    for (const perm of results) {
      // Generate slug from name
      const slug = perm.name.toUpperCase().replace(/\s+/g, '_');
      await sequelize.query(
        'UPDATE Permissions SET slug = ? WHERE id = ?',
        { replacements: [slug, perm.id] }
      );
      console.log(`✅ Updated permission "${perm.name}" -> "${slug}"`);
    }

    // 3. Now make it NOT NULL
    await queryInterface.changeColumn('Permissions', 'slug', {
      type: Sequelize.STRING,
      allowNull: false
    });
    console.log('✅ Made slug column NOT NULL');

    // 4. Add unique index
    await queryInterface.addIndex('Permissions', ['slug'], {
      unique: true,
      name: 'permissions_slug_unique'
    });
    console.log('✅ Added unique index on slug');
  },

  down: async (queryInterface, Sequelize) => {
    // Remove index
    await queryInterface.removeIndex('Permissions', 'permissions_slug_unique');

    // Remove column
    await queryInterface.removeColumn('Permissions', 'slug');
  }
};
